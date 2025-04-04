export interface PromotionRequest {
    name: string;
    description: string;
    discountPercentage: number;
    startDate: string;
    endDate: string;
    applyToAll?: boolean;
    isActive?: boolean;
}

export interface PromotionResponse {
    id: string;
    name: string;
    description: string;
    discountPercentage: number;
    startDate: string;
    endDate: string;
    applyToAll?: boolean;
    isActive?: boolean;
    createdAt: string;
    updatedAt: string;
}