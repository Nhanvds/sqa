import { OrderStatusEnum } from "@/types/enums";
import { SortType } from "@/types/sort";

export interface PaginationParams {
    page: number;
    size: number;
    sortBy?: string;
    sortDirection?: SortType;
    search: string;
}

export interface Sort {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
}

export interface PageResponse<T> {
    content: T[];
    pageable: PaginationParams;
    totalElements: number;
    totalPages: number;
    last: boolean;
    size: number;
    number: number;
    sort: Sort;
    numberOfElements: number;
    first: boolean;
    empty: boolean;
}

export interface PaginationParamsExtra {
    page: number;
    size: number;
    sortBy?: string;
    sortDirection?: SortType;
    productName: string;
    customerEmail: string;
    status: OrderStatusEnum;
}