import {Col, Row, Select, Typography} from "antd";
import {ProductResponse} from "../../types/product";
import ProductItem from "./ProductItem";
import {Option} from "antd/es/mentions";
import '../../sass/home-page.scss';

const ProductList = ({ products, categoryName }: { products: ProductResponse[], categoryName: string }) => {
    return (
        <div>
            <Row justify="space-between" align="middle" className="product-list-header">
                <Col>
                    <p className="fw-600 fs-18 m-0">
                        {categoryName}
                    </p>
                </Col>

                <Col>
                    <div className="flex align-center">
                        <span className="mr-2">Sort by:</span>
                        <Select defaultValue="newest" style={{ width: 120 }}>
                            <Option value="newest">Newest</Option>
                            <Option value="oldest">Oldest</Option>
                            <Option value="priceAsc">Price Ascending</Option>
                            <Option value="priceDesc">Price Descending</Option>
                        </Select>
                    </div>
                </Col>
            </Row>
            <Row gutter={[24, 24]} className="product-list-grid">
                {products.map((product) => (
                    <Col key={product.id} xs={24} sm={12} md={10} lg={8} className="product-col">
                        <ProductItem product={product} />
                    </Col>
                ))}
            </Row>
        </div>
    );
};

export default ProductList;