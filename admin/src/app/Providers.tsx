"use client";

import { persistor, store } from "@/redux/store";
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/integration/react';

export default function Providers({ children }: Readonly<{ children: React.ReactNode }>) {
    return (
        <Provider store={store}>
            <PersistGate loading={null} persistor={persistor}>
                {children}
            </PersistGate>
        </Provider>
    )
}