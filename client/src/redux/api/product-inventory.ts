import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseResponse} from "../../types/base-response";
import {baseApi} from "./auth-api";
import {ProductInventoryResponse} from "../../types/product-inventory";

export const productInventoryApi = createApi({
    reducerPath: "productInventoryApi",
    baseQuery: fetchBaseQuery({baseUrl: baseApi}),
    endpoints: (builder) => ({
        getProductInventoryByProductId: builder.query<BaseResponse<ProductInventoryResponse[]>, string>({
            query: (productId: string) => ({
                url: `product-inventory?productId=${productId}`,
                method: "GET",
            }),
        }),
    }),
});

export const {useGetProductInventoryByProductIdQuery} = productInventoryApi;