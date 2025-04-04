import { BaseResponse } from "@/types/base-response";
import { LoginEmailRequest, LoginResponse } from "@/types/login";
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";


export const baseApi = process.env.NEXT_PUBLIC_API_BASE_URL;
// export const baseApi = "http://localhost:8080";


export const authApi = createApi({
    reducerPath: "authApi",
    baseQuery: fetchBaseQuery({ baseUrl: baseApi }),
    endpoints: (builder) => ({
        loginEmail: builder.mutation<BaseResponse<LoginResponse>, LoginEmailRequest>({
            query: (user) => ({
                url: "auth/login",
                method: "POST",
                body: user,
            }),
        }),
    }),
});

export const { useLoginEmailMutation } = authApi;