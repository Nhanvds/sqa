import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {baseApi} from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { PageResponse, PaginationParams } from "@/types/page";
import { ProductRequest, ProductResponse } from "@/types/product";
import { getToken } from "@/redux/api/user-api";

export const productApi = createApi({
    reducerPath: "productApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getProductFilter: builder.query<BaseResponse<PageResponse<ProductResponse>>, PaginationParams>({
            query: ({ page = 0, size = 10, sortBy = 'name', sortDirection = 'asc', search = '' }) => ({
                url: `product?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}&name=${search}`,
                method: "GET",
            }),
        }),
        getProductById: builder.query<BaseResponse<ProductResponse>, string>({
            query: (id) => ({
                url: `product/${id}`,
                method: 'GET',
            }),
        }),

        createProduct: builder.mutation<BaseResponse<string>, ProductRequest>({
            query: (newProduct) => ({
                url: 'product',
                method: 'POST',
                body: newProduct,
            }),
        }),

        updateProduct: builder.mutation<BaseResponse<string>, { id: string; product: ProductRequest }>({
            query: ({ id, product }) => ({
                url: `product/${id}`,
                method: 'PUT',
                body: product,
            }),
        }),

        deleteProduct: builder.mutation<BaseResponse<string>, string>({
            query: (id) => ({
                url: `product/${id}`,
                method: 'DELETE',
            }),
        }),
    }),
});

export const { useGetProductFilterQuery, useLazyGetProductFilterQuery, useCreateProductMutation, useDeleteProductMutation, useUpdateProductMutation } = productApi;