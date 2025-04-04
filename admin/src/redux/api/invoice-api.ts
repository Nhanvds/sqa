
import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { baseApi } from './auth-api';
import { getToken } from '@/redux/api/user-api';
import { BaseResponse } from '@/types/base-response';
import { InvoiceResponse } from '@/types/invoice';
import { PageResponse, PaginationParams } from '@/types/page';

export const invoiceApi = createApi({
    reducerPath: 'invoiceApi',
    baseQuery: fetchBaseQuery({
        baseUrl: baseApi,
        prepareHeaders: (headers) => {
            headers.set('Authorization', getToken());
            return headers;
        },
    }),
    endpoints: (builder) => ({
        getInvoice: builder.query<BaseResponse<PageResponse<InvoiceResponse>>, PaginationParams>({
            query: ({page, size, sortBy, sortDirection, search}) => ({
                url: 'invoices/getAll',
                method: 'GET',
                params: {
                    page,
                    size,
                    sortBy,
                    sortDirection,
                    customerEmail: search
                }
            }),
        }),
    }),
});

export const { useGetInvoiceQuery } = invoiceApi;