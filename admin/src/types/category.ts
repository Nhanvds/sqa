import { StatusEnum } from "@/types/enums";

export interface CategoryRequest {
    name: string;
    description: string;
    status: StatusEnum;
}

export interface CategoryResponse {
    id: string;
    name: string;
    description: string;
    status: StatusEnum;
    createdAt: Date;
    updatedAt: Date;
}
