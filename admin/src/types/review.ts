export interface ReviewResponse {
    id: string;
    orderId: string;
    productId: string;
    userReviewResponse: {
        id: string;
        name: string;
    };
    rating: number;
    content: string;
}

export interface ReviewRequest {
    productId: string;
    orderId: string;
    rating: number;
    content: string;
}