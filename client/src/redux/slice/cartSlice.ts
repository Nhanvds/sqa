import {CartItemResponse, CartResponse} from "../../types/cart";
import {createSlice, PayloadAction} from "@reduxjs/toolkit";


const initialState: { cart: CartResponse | null } = {
    cart: null,
};

const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {
        setCart(state, action: PayloadAction<CartResponse>) {
            state.cart = action.payload;
        },
        clearCart(state) {
            state.cart = null;
        },
        addCartItem(state, action: PayloadAction<CartItemResponse>) {
            if (state.cart) {
                state.cart.cartItems.push(action.payload);
            }
        },
        updateCartItem(state, action: PayloadAction<CartItemResponse>) {
            if (state.cart) {
                const index = state.cart.cartItems.findIndex(item => item.id === action.payload.id);
                if (index !== -1) {
                    state.cart.cartItems[index] = action.payload;
                }
            }
        },
        deleteCartItem(state, action: PayloadAction<string>) {
            if (state.cart) {
                state.cart.cartItems = state.cart.cartItems.filter(item => item.id !== action.payload);
            }
        }
    },
});

export const {setCart, deleteCartItem, updateCartItem, addCartItem, clearCart} = cartSlice.actions;
export default cartSlice.reducer;