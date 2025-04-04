import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "../../types/base-response";
import { ProductImageResponse } from "../../types/product-image";

export const productImageApi = createApi({
    reducerPath: "productImageApi",
    baseQuery: fetchBaseQuery({ baseUrl: baseApi }),
    endpoints: (builder) => ({
        getProductImageByProductId: builder.query<BaseResponse<ProductImageResponse[]>, string>({
            query: (productId) => ({
                url: `product-image?productId=${productId}`,
                method: "GET",
            })
        }),
    }),
});

export const { useGetProductImageByProductIdQuery } = productImageApi;