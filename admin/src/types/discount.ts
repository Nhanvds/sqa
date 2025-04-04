import {DiscountTypeEnum} from "./enums";

export interface DiscountResponse {
    id: string;
    code: string;
    discountType: DiscountTypeEnum;
    discountPercentage?: number;
    discountValue?: number;
    maxDiscountValue: number;
    minOrderValue: number;
    autoApply: boolean;
    maxUses: number;
    usedCount: number;
    expiryDate?: Date;
    startDate?: Date;
}

export interface DiscountRequest {
    code: string;
    discountType: DiscountTypeEnum;
    discountPercentage?: number;
    discountValue?: number;
    maxDiscountValue?: number;
    minOrderValue: number;
    maxUses: number;
    autoApply: boolean;
    expiryDate?: Date;
    startDate?: Date;
}