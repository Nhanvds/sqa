import { InvoiceStatusEnum } from "@/types/enums";

export interface InvoiceResponse {
    id: string;
    totalAmount: string;
    invoiceNumber: string;
    status: InvoiceStatusEnum;
    createdAt: Date;
    updatedAt: Date;
}