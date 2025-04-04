import { baseApi } from '@/redux/api/auth-api';
import { getToken } from '@/redux/api/user-api';
import { BaseResponse } from '@/types/base-response';
import { OrderStatusEnum } from '@/types/enums';
import { OrderRequest, OrderResponse, UpdateOrderRequest } from '@/types/order';
import { PageResponse, PaginationParamsExtra } from '@/types/page';
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const orderApi = createApi({
    reducerPath: "orderApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        createOrder: builder.mutation<BaseResponse<OrderResponse>, OrderRequest>({
            query: (orderRequest) => ({
                url: 'order/create',
                method: 'POST',
                body: orderRequest,
            }),
        }),

        updateOrder: builder.mutation<BaseResponse<OrderResponse>, { orderId: string; order: UpdateOrderRequest }>({
            query: ({ orderId, order }) => ({
                url: `order/admin/edit/${orderId}`,
                method: 'PUT',
                body: order,
            }),
        }),

        getOrdersByUserId: builder.query<BaseResponse<OrderResponse[]>, string>({
            query: (userId) => ({
                url: `order/user/${userId}`,
                method: 'GET',
            }),
        }),

        getOrdersForAdmin: builder.query<BaseResponse<PageResponse<OrderResponse>>, PaginationParamsExtra>({
            query: ({ productName, customerEmail, status, page = 0, size = 10, sortBy, sortDirection }) => ({
                url: `order/admin`,
                method: 'GET',
                params: {
                    productName,
                    customerEmail,
                    status : status === OrderStatusEnum.ALL ? undefined : status,
                    page,
                    size,
                    sortBy,
                    sortDirection
                },
            }),
        }),

    }),
});

export const {
    useCreateOrderMutation,
    useUpdateOrderMutation,
    useGetOrdersForAdminQuery
} = orderApi;
