import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {baseApi} from "./auth-api";
import {BaseResponse} from "../../types/base-response";
import {CartItemRequest, CartItemResponse, CartResponse} from "../../types/cart";

export const cartApi = createApi({
    reducerPath: "cartApi",
    baseQuery: fetchBaseQuery({baseUrl: baseApi}),
    endpoints: (builder) => ({
        getCart: builder.query<BaseResponse<CartResponse>, string>({
            query: (userId: string) => ({
                url: `cart?userId=${userId}`,
                method: "GET",
            }),
        }),

        addCartItem: builder.mutation<BaseResponse<CartItemResponse>, CartItemRequest>({
            query: (cartItemRequest) => ({
                url: 'cart',
                method: 'POST',
                body: cartItemRequest,
            }),
        }),

        updateCartItem: builder.mutation<BaseResponse<CartItemResponse>, {
            id: string;
            cartItemRequest: CartItemRequest
        }>({
            query: ({id, cartItemRequest}) => ({
                url: `cart/${id}`,
                method: 'PUT',
                body: cartItemRequest,
            }),
        }),

        deleteCartItem: builder.mutation<BaseResponse<void>, { id: string; cartId: string }>({
            query: ({id}) => ({
                url: `cart/${id}`,
                method: 'DELETE',
            }),
        }),
    }),
});


export const {
    useGetCartQuery,
    useAddCartItemMutation,
    useUpdateCartItemMutation,
    useDeleteCartItemMutation,
} = cartApi;
