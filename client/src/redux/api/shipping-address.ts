import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseResponse} from "../../types/base-response";
import {baseApi} from "./auth-api";
import {ShippingAddressRequest, ShippingAddressResponse} from "../../types/shipping-address";

export const shippingAddressApi = createApi({
    reducerPath: "shippingAddressApi",
    baseQuery: fetchBaseQuery({ baseUrl: baseApi }),
    endpoints: (builder) => ({
        createShippingAddress: builder.mutation<BaseResponse<ShippingAddressResponse>, ShippingAddressRequest>({
            query: (shippingAddressRequest) => ({
                url: "shipping-address",
                method: "POST",
                body: shippingAddressRequest,
            }),
        }),

        updateShippingAddress: builder.mutation<BaseResponse<ShippingAddressResponse>, { id: string; shippingAddressRequest: ShippingAddressRequest }>({
            query: ({ id, shippingAddressRequest }) => ({
                url: `shipping-address/${id}`,
                method: "PUT",
                body: shippingAddressRequest,
            }),
        }),

        deleteShippingAddress: builder.mutation<BaseResponse<void>, string>({
            query: (id: string) => ({
                url: `shipping-address/${id}`,
                method: "DELETE",
            }),
        }),

        getShippingAddressByUserId: builder.query<BaseResponse<ShippingAddressResponse[]>, string>({
            query: (userId: string) => ({
                url: `shipping-address/${userId}`,
                method: "GET",
            }),
        }),
    }),
});

export const {
    useCreateShippingAddressMutation,
    useUpdateShippingAddressMutation,
    useDeleteShippingAddressMutation,
    useGetShippingAddressByUserIdQuery,
} = shippingAddressApi;