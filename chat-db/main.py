import os
import mysql.connector
from dotenv import load_dotenv
import re
import json
from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain
from langchain.llms.base import LLM
from typing import Optional, List
from groq import Groq
import warnings
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

warnings.filterwarnings("ignore", category=DeprecationWarning)

load_dotenv()

DATABASE_SCHEMA = """
Schema:
- Table: users
  - id (UUID, Primary Key)
  - email (String, Unique, Not Null)
  - password (String)
  - name (String)
  - google_id (String)
  - is_active (Boolean)
  - verification_token (String)
  - loyalty_tier (Enum)
  - roles (Set of Enum)
  - created_at (Timestamp)
  - updated_at (Timestamp)

- Table: products
  - id (UUID, Primary Key)
  - name (String, Not Null)
  - description (Lob)
  - price (BigDecimal, Not Null)
  - category_id (UUID, Foreign Key to categories.id)
  - rating (Double)
  - main_image (String)
  - is_active (Boolean, Default: true)
  - created_at (Timestamp)
  - updated_at (Timestamp)

- Table: sizes
  - id (UUID, Primary Key)
  - name (String, Not Null)

- Table: orders
  - id (UUID, Primary Key)
  - user_id (UUID, Foreign Key to users.id)
  - user_discount_id (UUID, Foreign Key to user_discounts.id)
  - shipping_address_id (UUID, Foreign Key to shipping_addresses.id)
  - status (Enum, Not Null)
  - invoice_id (UUID, Foreign Key to invoices.id)
  - total_price_before_discount (BigDecimal)
  - total_price_after_discount (BigDecimal)
  - created_at (Timestamp)
  - updated_at (Timestamp)

- Table: order_items
  - id (UUID, Primary Key)
  - order_id (UUID, Foreign Key to orders.id)
  - product_id (UUID, Foreign Key to products.id, Not Null)
  - size_id (UUID, Foreign Key to sizes.id, Not Null)
  - quantity (Integer, Not Null)
  - promotion_id (UUID, Foreign Key to promotions.id)
  - price (BigDecimal, Not Null)

- Table: discounts
  - id (UUID, Primary Key)
  - code (String, Unique, Not Null, Length: 6-20)
  - discount_type (Enum, Not Null)
  - discount_percentage (BigDecimal, >= 0.0)
  - discount_value (BigDecimal, >= 0.0)
  - max_discount_value (BigDecimal, >= 0.0, Not Null)
  - min_order_value (BigDecimal, Not Null)
  - max_uses (Integer, Not Null)
  - used_count (Integer, Not Null, Default: 0)
  - expiry_date (Timestamp)
  - start_date (Timestamp)
  - auto_apply (Boolean)
  - created_at (Timestamp)

- Table: promotions
  - id (UUID, Primary Key)
  - name (String)
  - description (String)
  - discount_percentage (BigDecimal)
  - start_date (Timestamp, Not Null)
  - end_date (Timestamp, Not Null)
  - is_active (Boolean)
  - apply_to_all (Boolean, Not Null, Default: false)
  - created_at (Timestamp)
  - updated_at (Timestamp)

- Table: promotion_products
  - id (UUID, Primary Key)
  - promotion_id (UUID, Foreign Key to promotions.id)
  - product_id (UUID, Foreign Key to products.id)

- Table: product_inventory
  - id (UUID, Primary Key)
  - product_id (UUID, Foreign Key to products.id, Not Null)
  - size_id (UUID, Foreign Key to sizes.id, Not Null)
  - quantity (Integer, Not Null)

- Table: carts
  - id (UUID, Primary Key)
  - user_id (UUID, Foreign Key to users.id)
  - created_at (Timestamp)
  - updated_at (Timestamp)

- Table: cart_items
  - id (UUID, Primary Key)
  - cart_id (UUID, Foreign Key to carts.id)
  - product_id (UUID, Foreign Key to products.id)
  - size_id (UUID, Foreign Key to sizes.id)
  - quantity (Integer, Not Null)

- Table: categories
  - id (UUID, Primary Key)
  - name (String, Not Null)
  - description (String)
  - is_active (Boolean, Not Null)
  - created_at (Timestamp, Not Null)
  - updated_at (Timestamp, Not Null)
"""

class GroqLLM(LLM):
    api_key: str
    model: str = "llama-3.2-11b-vision-preview"

    @property
    def _llm_type(self) -> str:
        return "groq-llm"

    def _call(self, prompt: str, stop: Optional[List[str]] = None) -> str:
        client = Groq(api_key=self.api_key)
        try:
            chat_completion = client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user", "content": prompt}],
            )
            response = chat_completion.choices[0].message.content.strip()
            return response
        except Exception as e:
            print(f"Lỗi khi gọi Groq API: {e}")
            return "Có lỗi xảy ra khi xử lý yêu cầu của bạn."

    async def _acall(self, prompt: str, stop: Optional[List[str]] = None) -> str:
        raise NotImplementedError("GroqLLM does not support async yet.")

def connect_to_mysql():
    try:
        connection = mysql.connector.connect(
            host=os.getenv('DB_HOST'),
            user=os.getenv('DB_USER'),
            password=os.getenv('DB_PASS'),
            database=os.getenv('DB_NAME')
        )
        if connection.is_connected():
            print("Kết nối tới MySQL thành công.")
            return connection
    except mysql.connector.Error as err:
        print(f"Lỗi kết nối MySQL: {err}")
        return None

def extract_sql(query_response):
    sql_match = re.search(r"```sql\s*(.*?)\s*```", query_response, re.DOTALL | re.IGNORECASE)
    if sql_match:
        return sql_match.group(1).strip()

    lines = query_response.splitlines()
    for i, line in enumerate(lines):
        if line.strip().lower().startswith("select"):
            return "\n".join(lines[i:]).strip()

    return ""

def generate_sql_query(user_input, llm):
    prompt_template = """
Bạn là một chuyên gia SQL. Dựa trên schema cơ sở dữ liệu dưới đây, hãy chuyển đổi câu hỏi người dùng thành một câu truy vấn SQL chính xác và chỉ trả về câu truy vấn SQL mà không có bất kỳ phần giải thích nào khác.

Schema cơ sở dữ liệu:
{schema}

Câu hỏi người dùng: {user_input}

Truy vấn SQL:
"""
    prompt = PromptTemplate(
        input_variables=["schema", "user_input"],
        template=prompt_template
    )

    chain = LLMChain(llm=llm, prompt=prompt)
    response = chain.run(schema=DATABASE_SCHEMA, user_input=user_input)
    print("Generated Response:", response)

    sql_query = extract_sql(response)

    if not sql_query:
        print("Không thể trích xuất truy vấn SQL từ phản hồi của mô hình.")
        return ""

    print("Generated SQL Query:", sql_query)
    return sql_query

def execute_query(query):
    connection = connect_to_mysql()
    if not connection:
        return "Không thể kết nối tới cơ sở dữ liệu."

    try:
        cursor = connection.cursor()
        cursor.execute(query)
        if query.strip().lower().startswith("select"):
            result = cursor.fetchall()
            columns = cursor.column_names
            cursor.close()
            connection.close()
            formatted_result = [dict(zip(columns, row)) for row in result]
            return formatted_result
        else:
            connection.commit()
            cursor.close()
            connection.close()
            return "Truy vấn đã được thực thi thành công."
    except mysql.connector.Error as err:
        print(f"Lỗi khi thực thi truy vấn: {err}")
        return f"Lỗi khi thực thi truy vấn: {err}"
    except Exception as e:
        print(f"Lỗi không xác định: {e}")
        return f"Lỗi không xác định: {e}"

def format_result_natural_language(result, user_input, llm):
    if not result:
        return "Hiện tại không có dữ liệu phù hợp với yêu cầu của bạn."

    data_json = json.dumps(result, ensure_ascii=False, default=str)
    prompt_template = """
Bạn là một trợ lý thông minh và chuyên nghiệp. Dựa trên dữ liệu truy vấn từ cơ sở dữ liệu dưới đây, hãy trả lời người dùng một cách tự nhiên và dễ hiểu bằng tiếng Việt.

Dữ liệu truy vấn:
{data_json}

Câu hỏi của người dùng:
{user_input}

Phản hồi:
"""
    prompt = PromptTemplate(
        input_variables=["data_json", "user_input"],
        template=prompt_template
    )

    chain = LLMChain(llm=llm, prompt=prompt)
    try:
        response = chain.run(data_json=data_json, user_input=user_input)
        return response.strip()
    except Exception as e:
        print(f"Lỗi khi tạo phản hồi ngôn ngữ tự nhiên: {e}")
        return "Có lỗi xảy ra khi xử lý yêu cầu của bạn."

def is_database_related(user_input, llm):
    prompt_template = """
Bạn là một trợ lý thông minh. Xác định xem câu hỏi sau đây có liên quan đến cơ sở dữ liệu và cần một truy vấn SQL hay không. Trả lời với "YES" nếu cần truy vấn SQL, hoặc "NO" nếu không.

Câu hỏi: "{user_input}"

Trả lời:
"""
    prompt = PromptTemplate(
        input_variables=["user_input"],
        template=prompt_template
    )

    chain = LLMChain(llm=llm, prompt=prompt)
    response = chain.run(user_input=user_input)
    print("Intent Classification Response:", response)

    if "yes" in response.lower():
        return True
    else:
        return False

def chat_with_db(user_input, sql_llm, nl_llm, intent_llm):
    is_db_related = is_database_related(user_input, intent_llm)
    if is_db_related:
        sql_query = generate_sql_query(user_input, sql_llm)
        if not sql_query:
            return "Không thể tạo truy vấn SQL từ câu hỏi của bạn."
        raw_result = execute_query(sql_query)
        if isinstance(raw_result, list):
            natural_language_response = format_result_natural_language(raw_result, user_input, nl_llm)
            return natural_language_response
        else:
            return raw_result
    else:
        prompt_template = """
Bạn là một trợ lý thông minh và chuyên nghiệp. Trả lời câu hỏi của người dùng một cách tự nhiên và dễ hiểu bằng tiếng Việt.

Câu hỏi của người dùng: {user_input}

Trả lời:
"""
        prompt = PromptTemplate(
            input_variables=["user_input"],
            template=prompt_template
        )

        chain = LLMChain(llm=nl_llm, prompt=prompt)
        try:
            response = chain.run(user_input=user_input)
            return response.strip()
        except Exception as e:
            print(f"Lỗi khi tạo phản hồi ngôn ngữ tự nhiên: {e}")
            return "Có lỗi xảy ra khi xử lý yêu cầu của bạn."

class ChatRequest(BaseModel):
    user_input: str

class ChatResponse(BaseModel):
    response: str

app = FastAPI(title="Chat DB API")

origins = [
    "http://localhost",
    "http://localhost:3000",
    "http://localhost:8080",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

groq_api_key = os.getenv('GROQ_API_KEY')
if not groq_api_key:
    raise ValueError("Thiếu GROQ_API_KEY trong biến môi trường.")

sql_llm = GroqLLM(api_key=groq_api_key, model="llama-3.2-11b-vision-preview")
nl_llm = GroqLLM(api_key=groq_api_key, model="llama-3.2-11b-vision-preview")
intent_llm = GroqLLM(api_key=groq_api_key, model="llama-3.2-11b-vision-preview")

@app.post("/chat", response_model=ChatResponse)
def chat_endpoint(chat_request: ChatRequest):
    user_input = chat_request.user_input
    if not user_input:
        raise HTTPException(status_code=400, detail="user_input không được để trống.")
    
    response = chat_with_db(user_input, sql_llm, nl_llm, intent_llm)
    return ChatResponse(response=response)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
