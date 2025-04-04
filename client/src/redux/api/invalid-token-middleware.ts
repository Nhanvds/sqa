import {Middleware, isRejectedWithValue} from '@reduxjs/toolkit';
import {authApi} from './auth-api';

const hasStatus = (payload: any): payload is { status: number } => {
    return payload && typeof payload.status === 'number';
};

export const invalidTokenMiddleware: Middleware = (store) => (next) => async (action) => {
    const isAuthApiAction = authApi.endpoints.loginEmail.matchRejected(action) || authApi.endpoints.register.matchRejected(action);
    if (isRejectedWithValue(action) && hasStatus(action.payload) && action.payload.status === 401) {
        if (!isAuthApiAction) {
            localStorage.removeItem('token');
        }
    }

    return next(action);
};