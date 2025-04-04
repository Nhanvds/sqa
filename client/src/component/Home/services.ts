import { CartItemResponse, CartResponse } from "../../types/cart";
import { ProductResponse } from "../../types/product";

export const calculateItemTotal = (item: CartItemResponse) => {
    const discountedPrice = item.quantity * calculateProductItem(item.product);
    return discountedPrice;
};

export const calculateSubtotal = (cartData?: CartResponse | null) => {
    return cartData?.cartItems.reduce((total, item) => total + calculateItemTotal(item), 0) ?? 0;
};

export const calculateProductItem = (product: ProductResponse) => {
    return product.price * (1 - product.discountPercentage / 100);
}

