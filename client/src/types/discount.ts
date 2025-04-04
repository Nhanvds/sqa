import {DiscountTypeEnum} from "./enums";

export interface DiscountResponse {
    id: string;

    discountType: DiscountTypeEnum;

    discountPercentage?: number;

    discountValue?: number;

    maxDiscountValue: number;

    minOrderValue: number;

    maxUses: number;

    usedCount: number;

    expiryDate?: Date;

    startDate?: Date;
}