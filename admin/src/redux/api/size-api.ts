import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { baseApi } from "./auth-api";
import { CategoryRequest, CategoryResponse } from "@/types/category";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { SizeRequest, SizeResponse } from "@/types/size";

export const sizeApi = createApi({
    reducerPath: "sizeApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getAllSize: builder.query<BaseResponse<SizeResponse[]>, void>({
            query: () => ({
                url: "size/get-all",
                method: "GET",
            }),
        }),

        createSize: builder.mutation<BaseResponse<SizeResponse>, SizeRequest>({
            query: (size) => ({
                url: 'size',
                method: 'POST',
                body: size
            }),
        }),

        updateSize: builder.mutation<BaseResponse<CategoryResponse>, {
            id: string;
            size: SizeRequest;
        }>({
            query: (size) => ({
                url: 'category/' + size.id,
                method: 'PUT',
                body: size.size
            }),
        }),

        deleteSize: builder.mutation<BaseResponse<SizeResponse>, void>({
            query: (id) => ({
                url: `size/${id}`,
                method: 'DELETE',
            }),
        }),
    }),
});

export const { useCreateSizeMutation, useUpdateSizeMutation, useDeleteSizeMutation, useGetAllSizeQuery } = sizeApi;