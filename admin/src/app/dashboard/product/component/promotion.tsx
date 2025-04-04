import { formatDateString } from "@/constants/date";
import { PromotionResponse } from "@/types/promotion";


interface PromotionDetailProps {
    promotion: PromotionResponse;
}

const PromotionDetail: React.FC<PromotionDetailProps> = ({ promotion }) => {
    return (
        <div className="w-96 p-4 border rounded-md shadow-lg bg-white dark:bg-gray-800">
            <div className="flex flex-col space-y-3">
                <div className="text-lg font-semibold text-gray-800 dark:text-gray-200">
                    Promotion Details
                </div>
                <div className="grid grid-cols-3 gap-y-2">
                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        Name:
                    </span>
                    <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                        {promotion.name}
                    </span>

                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        Description:
                    </span>
                    <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                        {promotion.description}
                    </span>

                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        Discount:
                    </span>
                    <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                        {promotion.discountPercentage}%
                    </span>
                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        Auto Apply:
                    </span>
                    <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                        {promotion.applyToAll ? 'Yes' : 'No'}
                    </span>

                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        Start Date:
                    </span>
                    <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                        {formatDateString(promotion.startDate)}
                    </span>

                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        End Date:
                    </span>
                    <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                        {formatDateString(promotion.endDate)}
                    </span>

                    <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                        Status:
                    </span>
                    <span className="col-span-2">
                        <span className={`text-sm px-2 py-1 rounded-md ${promotion.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                            {promotion.isActive ? 'Active' : 'Inactive'}
                        </span>
                    </span>
                </div>
            </div>
        </div>
    );
};

export default PromotionDetail;
