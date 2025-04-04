import {ShippingAddressResponse} from "./shipping-address";
import {SizeResponse} from "./size";
import {ProductResponse} from "./product";
import {OrderStatusEnum} from "./enums";
import { PromotionResponse } from "@/types/promotion";
import { User } from "@/types/user-info";
import { DiscountResponse } from "@/types/discount";

export interface OrderResponse {
    id: string;
    user: User;
    shippingAddress: ShippingAddressResponse;
    userDiscount?: {
        id: string;
        discount: DiscountResponse;
        usesCount: number;
    }
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
    status: OrderStatusEnum;
}

export interface OrderItemResponse {
    id: string;
    productResponse: ProductResponse;
    size: SizeResponse;
    quantity: number;
    promotion: PromotionResponse
    price: number;
}
