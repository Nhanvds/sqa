import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import {BaseResponse} from "../../types/base-response";
import {UserInfo} from "../../types/user-info";

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
    }),
});

export const { useLazyGetUserInfoQuery } = userApi;