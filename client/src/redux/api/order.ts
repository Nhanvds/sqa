import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseResponse} from "../../types/base-response";
import {baseApi} from "./auth-api";
import {OrderRequest, OrderResponse, UpdateOrderRequest} from "../../types/order";

export const orderApi = createApi({
    reducerPath: "orderApi",
    baseQuery: fetchBaseQuery({ baseUrl: baseApi }),
    endpoints: (builder) => ({
        getOrdersByUserId: builder.query<BaseResponse<OrderResponse[]>, string>({
            query: (userId: string) => ({
                url: `order/user/${userId}`,
                method: "GET",
            }),
        }),

        createOrderFromCart: builder.mutation<BaseResponse<OrderResponse>, OrderRequest>({
            query: (orderRequest) => ({
                url: "order/create",
                method: "POST",
                body: orderRequest,
            }),
        }),

        clientEditOrder: builder.mutation<BaseResponse<OrderResponse>, UpdateOrderRequest>({
            query: (clientUpdateOrderRequest) => ({
                url: "order/client/edit",
                method: "PUT",
                body: clientUpdateOrderRequest,
            }),
        }),
    }),
});

export const {
    useGetOrdersByUserIdQuery,
    useCreateOrderFromCartMutation,
    useClientEditOrderMutation,
} = orderApi;