import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {baseApi} from "./auth-api";
import { CategoryRequest, CategoryResponse } from "@/types/category";
import { BaseResponse } from "@/types/base-response";
import { getToken } from "@/redux/api/user-api";
import { PageResponse, PaginationParams } from "@/types/page";

export const categoryApi = createApi({
    reducerPath: "categoryApi",
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", getToken());
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getAllCategories: builder.query<BaseResponse<CategoryResponse[]>, void>({
            query: () => ({
                url: "category",
                method: "GET",
            }),
        }),

        getAllCategory: builder.query<BaseResponse<PageResponse<CategoryResponse>>, PaginationParams>({
            query: ({ page = 0, size = 10, sortBy = 'name', sortDirection = 'asc', search = '' }) => ({
                url: `category/page?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}&name=${search}`,
                method: "GET",
            }),
        }),

        createCategory: builder.mutation<BaseResponse<CategoryResponse>, CategoryRequest>({
            query: (category) => ({
                url: 'category',
                method: 'POST',
                body: category
            }),
        }),

        updateCategory: builder.mutation<BaseResponse<CategoryResponse>, {
            id: string;
            category: CategoryRequest;
        }>({
            query: (category) => ({
                url: 'category/' + category.id,
                method: 'PUT',
                body: category.category
            }),
        }),

        deleteCategory: builder.mutation<BaseResponse<CategoryResponse>, string>({
            query: (id) => ({
                url: `category/${id}`,
                method: 'DELETE',
            }),
        }),
    }),
});

export const { useGetAllCategoryQuery, useCreateCategoryMutation, useUpdateCategoryMutation, useDeleteCategoryMutation, useGetAllCategoriesQuery } = categoryApi;