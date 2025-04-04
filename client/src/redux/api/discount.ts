import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseResponse} from "../../types/base-response";
import {baseApi} from "./auth-api";
import {DiscountResponse} from "../../types/discount";

export const discountApi = createApi({
    reducerPath: "discountApi",
    baseQuery: fetchBaseQuery({baseUrl: baseApi}),
    endpoints: (builder) => ({
        getAutoApply: builder.query<BaseResponse<DiscountResponse[]>, string>({
            query: (userId: string) => ({
                url: "discounts/auto-apply?userId=" + userId,
                method: "GET"
            }),
        }),

        getDiscount: builder.query<BaseResponse<DiscountResponse>, { code: string, userId: string }>({
            query: ({code, userId}) => ({
                url: "discounts/get-code?code=" + code + "&userId=" + userId,
                method: "GET"
            }),
        }),
    }),
});

export const {useGetAutoApplyQuery, useLazyGetDiscountQuery} = discountApi;