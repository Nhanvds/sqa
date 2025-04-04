export enum StatusEnum {
    ACTIVE = "ACTIVE",
    INACTIVE = "INACTIVE",
    DELETE = "DELETE"
}

export enum LoyaltyTierEnum {
    BRONZE = "BRONZE",
    SILVER = "SILVER",
    GOLD = "GOLD",
    PLATINUM = "PLATINUM"
}

export enum RoleEnum {
    ADMIN = "ADMIN",
    CUSTOMER = "CUSTOMER",
    STAFF = "STAFF",
    SHIPPER = "SHIPPER",
    CHATBOT = "CHATBOT"
}

export enum DiscountTypeEnum {
    PERCENTAGE = "PERCENTAGE",
    VALUE = "VALUE"
}

export enum OrderStatusEnum {
    ALL = "ALL", 
    PENDING = "PENDING",
    CONFIRMED = "CONFIRMED",
    SHIPPING = "SHIPPING",
    DELIVERED = "DELIVERED",
    COMPLETED = "COMPLETED",
    CANCELLED = "CANCELLED"
}

export enum PaymentMethodEnum {
    TRANSFER = "TRANSFER",
    CASH = "CASH"
}

export enum PaymentStatusEnum {
    PENDING = "PENDING",
    COMPLETED = "COMPLETED",
    FAILED = "FAILED",
    CANCELED = "CANCELED"
}

export enum InvoiceStatusEnum {
    UNPAID = "UNPAID",
    PAID = "PAID",
    CANCELED = "CANCELED"
}

export enum ChatRoomStatus {
    OPEN = "OPEN",
    CLOSED = "CLOSED",
    PENDING = "PENDING",
    ARCHIVED = "ARCHIVED"
}