export interface ShippingAddressRequest {
    userId: string;
    recipientName: string;
    phoneNumber: string;
    addressDetail: string;
    city: string;
    district: string;
    ward: string;
    country: string;
    isDefault?: boolean;
}

export interface ShippingAddressResponse {
    id: string;
    recipientName: string;
    phoneNumber: string;
    addressDetail: string;
    city: string;
    district: string;
    ward: string;
    country: string;
    isDefault: boolean;
}

export interface GHTKFee {
    fee: number;
    message: string;
    success: boolean;
}

export interface GHTKFeeRequest {
    addressDetail: string;
    city: string;
    district: string;
    ward: string;
    value: number;
}
