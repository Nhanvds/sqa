import {configureStore} from "@reduxjs/toolkit"
import {authApi} from "./api/auth-api"
import {setupListeners} from "@reduxjs/toolkit/query"
import {userApi} from "./api/user-api";
import userReducer from './slice/userSlice';
import cartReducer from './slice/cartSlice';
import {categoryApi} from "./api/category-api";
import {productApi} from "./api/product-api";
import {invalidTokenMiddleware} from "./api/invalid-token-middleware";
import {productInventoryApi} from "./api/product-inventory";
import {cartApi} from "./api/cart";
import {discountApi} from "./api/discount";
import {orderApi} from "./api/order";
import {shippingAddressApi} from "./api/shipping-address";
import {invoiceApi} from "./api/invoice";
import { reviewApi } from "./api/review";
import { productImageApi } from "./api/product-image";
import { chatApi } from "./api/chat-api";

export const store = configureStore({
    reducer: {
        [authApi.reducerPath]: authApi.reducer,
        [userApi.reducerPath]: userApi.reducer,
        [categoryApi.reducerPath]: categoryApi.reducer,
        [productApi.reducerPath]: productApi.reducer,
        [productInventoryApi.reducerPath]: productInventoryApi.reducer,
        [cartApi.reducerPath]: cartApi.reducer,
        [discountApi.reducerPath]: discountApi.reducer,
        [orderApi.reducerPath]: orderApi.reducer,
        [shippingAddressApi.reducerPath]: shippingAddressApi.reducer,
        [invoiceApi.reducerPath]: invoiceApi.reducer,
        [reviewApi.reducerPath]: reviewApi.reducer,
        [productImageApi.reducerPath]: productImageApi.reducer,
        [chatApi.reducerPath]: chatApi.reducer,
        user: userReducer,
        cart: cartReducer,
    },

    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(authApi.middleware, userApi.middleware, categoryApi.middleware,
            productApi.middleware, productInventoryApi.middleware, cartApi.middleware,
            discountApi.middleware, shippingAddressApi.middleware, invoiceApi.middleware, orderApi.middleware,
            reviewApi.middleware, productImageApi.middleware, chatApi.middleware
        ).concat(invalidTokenMiddleware),
})

export type RootState = ReturnType<typeof store.getState>;
setupListeners(store.dispatch)