import {ProductResponse} from "./product";
import {SizeResponse} from "./size";

export interface CartItemResponse {
    id: string;
    product: ProductResponse;
    size: SizeResponse;
    quantity: number;
}

export interface CartResponse {
    id: string;
    cartItems: CartItemResponse[];
}

export interface CartItemRequest {
    cartId: string;
    productId: string;
    sizeId: string;
    quantity: number;
}