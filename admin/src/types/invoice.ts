import {InvoiceStatusEnum, PaymentMethodEnum, PaymentStatusEnum} from "./enums";

export interface InvoiceResponse {
    id: string;
    payment?: PaymentResponse;
    totalAmount: number;
    invoiceNumber: string;
    status: InvoiceStatusEnum;
    createdAt: Date;
    updatedAt: Date;
}

export interface PaymentResponse {
    id: string;
    paymentMethod: PaymentMethodEnum;
    code: string;
    qrCodeUrl: string;
    amount: number;
    paymentStatus: PaymentStatusEnum;
    paymentDate: Date;
}