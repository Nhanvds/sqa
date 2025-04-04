import { StatusEnum } from "./enums";

export interface CategoryRequest {
    name: string;
    description: string;
    isActive: boolean;
}

export interface CategoryResponse {
    id: string;
    name: string;
    description: string;
    status: StatusEnum;
    createdAt: Date;
    updatedAt: Date;
}
