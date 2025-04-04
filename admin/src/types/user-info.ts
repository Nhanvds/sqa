import { LoyaltyTierEnum, RoleEnum, StatusEnum } from "./enums";

export interface PhoneNumber {
    id: string;
    phoneNumber: string;
}

export interface Address {
    id: string;
    addressDetail: string;
    country: string;
}

export interface UserInfo {
    id: string;
    email: string;
    name: string;
    status: StatusEnum;
    loyaltyTier: LoyaltyTierEnum;
    roles: RoleEnum[];
    createdAt: Date;
    updatedAt: Date;
    phoneNumbers?: PhoneNumber[];
    addresses?: Address[];
}

export interface User {
    id: string;
    email: string;
    name: string;
    status: StatusEnum;
    loyaltyTier: LoyaltyTierEnum;
    roles: RoleEnum[];
    createdAt: Date;
    updatedAt: Date;
    phoneNumbers?: PhoneNumber[];
    addresses?: Address[];
}

export interface UserRequest {
    email: string;
    name: string;
    status: StatusEnum;
    loyaltyTier: LoyaltyTierEnum;
    roles: RoleEnum[];
}