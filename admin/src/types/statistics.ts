export interface ProductStatistic {
    date: string;
    size: string;
    price: number;
    quantity: number;
    discountPercentage: number;
}

export interface ProductRevenueResponse {
    productName: string;
    statistics: ProductStatistic[];
}

export interface CategoryStatistic {
    productName: string;
    date: string;
    size: string;
    price: number;
    quantity: number;
    discountPercentage: number;
}

export interface CategoryRevenueResponse {
    categoryName: string;
    statistics: CategoryStatistic[];
}

export interface CustomerStatistic {
    orderId: string;
    value: number;
    date: string;
}

export interface CustomerRevenueResponse {
    name: string;
    email: string;
    totalOrder: number;
    totalValue: number;
    statistics: CustomerStatistic[];
}
