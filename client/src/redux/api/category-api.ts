import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseResponse} from "../../types/base-response";
import {baseApi} from "./auth-api";
import {CategoryResponse} from "../../types/category";

export const categoryApi = createApi({
    reducerPath: "categoryApi",
    baseQuery: fetchBaseQuery({ baseUrl: baseApi }),
    endpoints: (builder) => ({
        getAllCategory: builder.query<BaseResponse<CategoryResponse[]>, void>({
            query: () => ({
                url: "category",
                method: "GET"
            }),
        }),
    }),
});

export const { useGetAllCategoryQuery } = categoryApi;