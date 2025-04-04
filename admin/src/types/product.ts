import { PromotionResponse } from "@/types/promotion";
import {CategoryResponse} from "./category";
import { StatusEnum } from "@/types/enums";

export interface ProductResponse {
    id: string;
    name: string;
    description: string;
    price: number;
    promotionResponse: PromotionResponse | null;
    categoryResponse: CategoryResponse;
    promotions: PromotionResponse[];
    rating: number;
    status: StatusEnum;
    discountPercentage: number;
    mainImage: string;
    createdAt: Date;
    updatedAt: Date;
}

export interface ProductRequest {
    name: string;
    description?: string;
    price: number;
    categoryId: string;
    status: StatusEnum;
    promotionIds?: string[];
    mainImage?: string;
}
