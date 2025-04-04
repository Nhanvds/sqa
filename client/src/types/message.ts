import { ChatRoomStatus } from "./enums";
import { UserInfo } from "./user-info";

export interface MessageResponse {
    id: string;
    sender: UserInfo;
    content: string;
    timestamp: Date;
}

export interface MessageRequest {
    chatRoomId: string;
    content: string;
}

export interface ChatRoomResponse {
    id: string;
    customerId: string;
    status: ChatRoomStatus;
    messages: MessageResponse[];
}