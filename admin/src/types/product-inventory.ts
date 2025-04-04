import {SizeResponse} from "./size";

export interface ProductInventoryResponse {
    id: string;
    idProduct: string;
    size: SizeResponse;
    quantity: number;
}

export interface ProductInventoryRequest {
    idProduct: string;
    idSize: string;
    quantity: number;
}
