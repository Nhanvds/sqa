import { OrderItemResponse } from "@/types/order";

const OrderItemDetailsPopover = ({ orderItem }: { orderItem: OrderItemResponse }) => {
    const { productResponse, size, quantity, promotion, price } = orderItem;

    return (
        <div className="flex flex-col space-y-3 min-w-96">
            <div className="text-lg font-semibold text-gray-800 dark:text-gray-200">
                Order Item Details
            </div>
            <div className="grid grid-cols-3 gap-y-2">
                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Product Name:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {productResponse.name}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Product Description:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {productResponse.description}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Category:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {productResponse.categoryResponse.name}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Size:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {size.name.toLocaleUpperCase()}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Quantity:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {quantity}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Price:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {price} đ
                </span>

                {/* <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Promotion:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {promotion.name} ({promotion.discountPercentage}% off)
                </span> */}

                {/* <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Product Status:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {productResponse.isActive ? 'Active' : 'Inactive'}
                </span> */}
            </div>
        </div>
    );
};

export default OrderItemDetailsPopover;
