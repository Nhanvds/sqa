import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { BaseResponse } from "@/types/base-response";
import { User, UserInfo, UserRequest } from "@/types/user-info";
import { PageResponse, PaginationParams } from "@/types/page";

export const getToken = () => {
    return `Bearer ${localStorage.getItem("token")}`;
};
export const userApi = createApi({
    reducerPath: "userApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getUserInfo: builder.query<BaseResponse<UserInfo>, void>({
            query: () => ({
                url: "auth/get-user",
                method: "GET"
            })
        }),
        getAllUser: builder.query<BaseResponse<PageResponse<User>>, PaginationParams>({
            query: ({ page = 0, size = 10, sortBy = 'name', sortDirection = 'asc', search = '' }) => ({
                url: `users?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}&name=${search}`,
                method: "GET",
            }),
        }),
        getUsers: builder.query<BaseResponse<User[]>, void>({
            query: () => ({
                url: "users/get-all",
                method: "GET",
            }),
        }),
        createUser: builder.mutation<BaseResponse<User>, UserRequest>({
            query: (body) => ({
                url: "users",
                method: "POST",
                body,
            }),
        }),

        updateUser: builder.mutation<BaseResponse<User>, {
            id: string;
            user: UserRequest;
        }>({
            query: (body) => ({
                url: `users/${body.id}`,
                method: "PUT",
                body: body.user,
            }),
        }),

        deleteUser: builder.mutation<BaseResponse<User>, string>({
            query: (id) => ({
                url: `users/${id}`,
                method: "DELETE",
            }),
        }),
    }),
});

export const { useLazyGetUserInfoQuery, useGetAllUserQuery, useGetUsersQuery, useCreateUserMutation, useDeleteUserMutation, useUpdateUserMutation } = userApi;