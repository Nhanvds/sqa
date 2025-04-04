import {createApi, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseResponse} from "../../types/base-response";
import {baseApi} from "./auth-api";
import {InvoiceResponse} from "../../types/invoice";

export const invoiceApi = createApi({
    reducerPath: "invoiceApi",
    baseQuery: fetchBaseQuery({ baseUrl: baseApi }),
    endpoints: (builder) => ({
        getInvoicesByOrderId: builder.query<BaseResponse<InvoiceResponse>, string>({
            query: (orderId: string) => ({
                url: `invoices/order?orderId=${orderId}`,
                method: "GET",
            }),
        }),
    }),
});

export const { useGetInvoicesByOrderIdQuery } = invoiceApi;