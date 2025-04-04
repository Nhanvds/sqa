import {ShippingAddressResponse} from "./shipping-address";
import {SizeResponse} from "./size";
import {ProductResponse} from "./product";
import {OrderStatusEnum} from "./enums";

export interface OrderResponse {
    id: string;
    shippingAddress: ShippingAddressResponse;
    status: OrderStatusEnum;
    totalPriceBeforeDiscount: number;
    totalPriceAfterDiscount: number;
    orderItems: OrderItemResponse[];
    createdAt: Date;
    updatedAt: Date;
}

export interface OrderRequest {
    userId: string;
    discountId?: string;
    shippingAddressId: string;
}

export interface UpdateOrderRequest {
    userId: string;
    discountId?: string;
    shippingAddressId: string;
}

export interface OrderItemResponse {
    id: string;
    productResponse: ProductResponse;
    size: SizeResponse;
    quantity: number;
    promotion: {
        discountPercentage: number;
    };
    price: number;
}