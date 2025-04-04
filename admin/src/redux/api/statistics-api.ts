import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { CategoryRevenueResponse, CustomerRevenueResponse, ProductRevenueResponse } from "@/types/statistics";

export const revenueApi = createApi({
    reducerPath: "revenueApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getProductRevenue: builder.query<BaseResponse<ProductRevenueResponse[]>, { startDate: string, endDate: string }>({
            query: ({ startDate, endDate }) => ({
                url: `revenue/product?startDate=${startDate}&endDate=${endDate}`,
                method: "GET",
            }),
        }),

        getCategoryRevenue: builder.query<BaseResponse<CategoryRevenueResponse[]>, { startDate: string, endDate: string }>({
            query: ({ startDate, endDate }) => ({
                url: `revenue/category?startDate=${startDate}&endDate=${endDate}`,
                method: "GET",
            }),
        }),

        getCustomerRevenue: builder.query<BaseResponse<CustomerRevenueResponse[]>, { startDate: string, endDate: string }>({
            query: ({ startDate, endDate }) => ({
                url: `revenue/customer?startDate=${startDate}&endDate=${endDate}`,
                method: "GET",
            }),
        }),
    }),
});

export const {
    useLazyGetProductRevenueQuery,
    useLazyGetCategoryRevenueQuery,
    useLazyGetCustomerRevenueQuery,
} = revenueApi;
