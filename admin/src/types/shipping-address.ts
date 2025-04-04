export interface ShippingAddressRequest {
    userId: string;
    recipientName: string;
    phoneNumber: string;
    addressDetail: string;
    country: string;
    isDefault?: boolean;
}

export interface ShippingAddressResponse {
    id: string;
    recipientName: string;
    phoneNumber: string;
    addressDetail: string;
    country: string;
    isDefault: boolean;
}