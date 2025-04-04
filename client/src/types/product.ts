import {CategoryResponse} from "./category";
import { StatusEnum } from "./enums";

export interface ProductResponse {
    id: string;
    name: string;
    description: string;
    price: number;
    category: CategoryResponse;
    rating: number;
    status: StatusEnum;
    discountPercentage: number;
    mainImage: string;
}
