import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { ProductInventoryRequest, ProductInventoryResponse } from "@/types/product-inventory";

export const productInventoryApi = createApi({
    reducerPath: "productInventoryApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getProductInventoryByProductIds: builder.query<BaseResponse<ProductInventoryResponse[]>, { productIds: string[] }>({
            query: (productIds) => ({
                url: `product-inventory/get-by-list-product-id`,
                method: "POST",
                body: productIds
            }),
        }),

        createProductInventory: builder.mutation<BaseResponse<ProductInventoryResponse>, ProductInventoryRequest>({
            query: (productInventory) => ({
                url: 'product-inventory',
                method: 'POST',
                body: productInventory
            }),
        }),

        updateProductInventory: builder.mutation<BaseResponse<ProductInventoryResponse>, {
            id: string;
            productInventory: ProductInventoryRequest;
        }>({
            query: (productInventory) => ({
                url: `product-inventory/${productInventory.id}`,
                method: 'PUT',
                body: productInventory.productInventory
            }),
        }),

        deleteProductInventory: builder.mutation<BaseResponse<String>, string>({
            query: (id) => ({
                url: `product-inventory/${id}`,
                method: 'DELETE',
            }),
        }),
    }),
});

export const { useCreateProductInventoryMutation, useUpdateProductInventoryMutation, useDeleteProductInventoryMutation, useLazyGetProductInventoryByProductIdsQuery } = productInventoryApi;