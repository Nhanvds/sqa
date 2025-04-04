import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { baseApi } from './auth-api';
import { BaseResponse } from '../../types/base-response';
import { ChatRoomResponse } from '../../types/message';

export const chatApi = createApi({
    reducerPath: 'chatApi',
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set("Authorization", `Bearer ${localStorage.getItem("token")}`);
            return headers;
        }
    }),
    endpoints: (builder) => ({
        sendMessage: builder.mutation<BaseResponse<void>, { chatRoomId: string, content: string }>({
            query: ({ chatRoomId, content }) => ({
                url: `/chat/sendMessage`,
                method: 'POST',
                body: { chatRoomId, content },
            }),
        }),

        getChatRoom: builder.query<BaseResponse<ChatRoomResponse>, string>({
            query: (customerId) => ({
                url: `/chat/getChatRoom/${customerId}`,
                method: 'GET',
            }),
        }),
    }),
});

export const {
    useSendMessageMutation,
    useGetChatRoomQuery,
} = chatApi;
