import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { PageResponse, PaginationParams } from "@/types/page";
import { ShippingAddressResponse } from "@/types/shipping-address";

export const shippingAddressApi = createApi({
    reducerPath: "shippingAddressApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getShippingAddressByUserId: builder.query<BaseResponse<ShippingAddressResponse[]>, string>({
            query: (userId) => ({
                url: `shipping-address/${userId}`,
                method: 'GET',
            }),
        }),

    }),
});

export const { useGetShippingAddressByUserIdQuery, useLazyGetShippingAddressByUserIdQuery } = shippingAddressApi;