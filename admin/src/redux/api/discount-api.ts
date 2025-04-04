import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { PageResponse, PaginationParams } from "@/types/page";
import { DiscountRequest, DiscountResponse } from "@/types/discount";

export const discountApi = createApi({
    reducerPath: "discountApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getDiscountSearchByCode: builder.query<BaseResponse<PageResponse<DiscountResponse>>, PaginationParams>({
            query: ({ page, size, sortBy, sortDirection, search  }) => ({
                url: `discounts/admin?code=${search}&page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}`,
                method: "GET"
            }),
        }),

        getDiscountByCode: builder.query<BaseResponse<DiscountResponse>, { code: string, userId: string }>({
            query: ({ code, userId }) => ({
                url: `discounts/get-code?code=${code}&userId=${userId}`,
                method: "GET"
            }),
        }),

        getDiscountById: builder.query<BaseResponse<DiscountResponse>, { userId: string, code: string }>({
            query: ({ userId, code }) => ({
                url: `discounts/get-discount?userId=${userId}&code=${code}`,
                method: "GET"
            }),
        }),

        createDiscount: builder.mutation<BaseResponse<DiscountResponse>, DiscountRequest>({
            query: (newDiscount) => ({
                url: `discounts`,
                method: "POST",
                body: newDiscount,
            }),
        }),

        updateDiscount: builder.mutation<BaseResponse<DiscountResponse>, { id: string, discount: DiscountRequest }>({
            query: ({ id, discount }) => ({
                url: `discounts/${id}`,
                method: "PUT",
                body: discount,
            }),
        }),

        deleteDiscount: builder.mutation<BaseResponse<void>, string>({
            query: (id) => ({
                url: `discounts/${id}`,
                method: "DELETE",
            }),
        }),

        getDiscountsByAutoApply: builder.query<BaseResponse<DiscountResponse[]>, { userId: string }>({
            query: ({ userId }) => ({
                url: `discounts/auto-apply?userId=${userId}`,
                method: "GET"
            }),
        }),

        getCurrentDiscount: builder.query<BaseResponse<DiscountResponse[]>, void>({
            query: () => ({
                url: `discounts/get-current-discount`,
                method: "GET"
            }),
        }),


    }),
});

export const {
    useGetDiscountSearchByCodeQuery,
    useGetDiscountByCodeQuery,
    useGetDiscountByIdQuery,
    useCreateDiscountMutation,
    useUpdateDiscountMutation,
    useDeleteDiscountMutation,
    useGetDiscountsByAutoApplyQuery,
    useGetCurrentDiscountQuery
} = discountApi;