import { SerializedError } from "@reduxjs/toolkit";
import { FetchBaseQueryError } from "@reduxjs/toolkit/query";

export const getErrorMessage = (error: FetchBaseQueryError | SerializedError | undefined): string => {
    if (!error) {
        return "An error occurred";
    }

    if ('data' in error) {
        if (typeof error.data === 'object' && error.data && 'message' in error.data) {
            return (error.data as any).message;
        }
        return JSON.stringify(error.data);
    }

    if ('message' in error) {
        return error.message || "An error occurred";
    }

    if ('error' in error) {
        return error.error;
    }

    return "An unknown error occurred";
};
