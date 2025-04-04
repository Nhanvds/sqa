import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "../../types/base-response";
import { ReviewRequest, ReviewResponse } from "../../types/review";
import { getToken } from "./user-api";

export const reviewApi = createApi({
    reducerPath: "reviewApi",
    baseQuery: fetchBaseQuery({ 
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getReviewByProductId: builder.query<BaseResponse<ReviewResponse[]>, string>({
            query: (productId) => ({
                url: `reviews?productId=${productId}`,
                method: "GET",
            })
        }),

        getReviewByOrderId: builder.query<BaseResponse<ReviewResponse[]>, string>({
            query: (orderId) => ({
                url: `reviews/order?orderId=${orderId}`
            })
        }),

        createReview: builder.mutation<BaseResponse<ReviewResponse>, ReviewRequest>({
            query: (review) => ({
                url: `reviews`,
                method: "POST",
                body: review,
            })
        }),

        updateReview: builder.mutation<BaseResponse<ReviewResponse>, {
            id: string;
            review: ReviewRequest;
        }>({
            query: (review) => ({
                url: `reviews/${review.id}`,
                method: "PUT",
                body: review.review,
            })
        }),

        deleteReview: builder.mutation<BaseResponse<ReviewResponse>, string>({
            query: (reviewId) => ({
                url: `reviews/${reviewId}`,
                method: "DELETE",
            })
        }),
    }),
});

export const { useGetReviewByProductIdQuery, useGetReviewByOrderIdQuery, useCreateReviewMutation, useUpdateReviewMutation, useDeleteReviewMutation } = reviewApi;