import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { baseApi } from './auth-api';
import { getToken } from '@/redux/api/user-api';
import { BaseResponse } from '@/types/base-response';

export const imageApi = createApi({
    reducerPath: 'imageApi',
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set('Authorization', getToken());
            return headers;
        },
    }),
    endpoints: (builder) => ({
        uploadImage: builder.mutation<BaseResponse<string>, FormData>({
            query: (formData) => ({
                url: 'images/upload',
                method: 'POST',
                body: formData,
            }),
        }),

        deleteImage: builder.mutation<BaseResponse<string>, string>({
            query: (publicId) => ({
                url: 'images/delete',
                method: 'DELETE',
                params: { publicId },
            }),
        }),
    }),
});

export const { useUploadImageMutation, useDeleteImageMutation } = imageApi;