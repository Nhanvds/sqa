import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { PageResponse, PaginationParams } from "@/types/page";
import { PromotionRequest, PromotionResponse } from "@/types/promotion";

export const promotionApi = createApi({
    reducerPath: "promotionApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getAllPromotion: builder.query<BaseResponse<PromotionResponse[]>, void>({
            query: () => ({
                url: `promotion/get-excluded-apply-to-all`,
                method: "GET"
            }),
        }),
        getPromotionFilter: builder.query<BaseResponse<PageResponse<PromotionResponse>>, PaginationParams>({
            query: ({ page = 0, size = 10, sortBy = 'name', sortDirection = 'asc', search = '' }) => ({
                url: `promotion/admin?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}&name=${search}`,
                method: "GET"
            }),
        }),
        getPromotionById: builder.query<BaseResponse<PromotionResponse>, string>({
            query: (id) => ({
                url: `promotion/client/${id}`,
                method: 'GET',
            }),
        }),

        createPromotion: builder.mutation<BaseResponse<void>, PromotionRequest>({
            query: (newPromotion) => ({
                url: 'promotion',
                method: 'POST',
                body: newPromotion,
            }),
        }),

        updatePromotion: builder.mutation<BaseResponse<void>, { id: string; promotion: PromotionRequest }>({
            query: ({ id, promotion }) => ({
                url: `promotion/${id}`,
                method: 'PUT',
                body: promotion,
            }),
        }),

        deletePromotion: builder.mutation<BaseResponse<void>, string>({
            query: (id) => ({
                url: `promotion/${id}`,
                method: 'DELETE',
            }),
        }),

    }),
});

export const { useGetPromotionFilterQuery, useLazyGetPromotionFilterQuery, useCreatePromotionMutation, useDeletePromotionMutation, useUpdatePromotionMutation, useGetAllPromotionQuery } = promotionApi;
