import { formatDateString } from "@/constants/date"
import { CategoryResponse } from "@/types/category"

const CategoryPopover = ({category}: {category: CategoryResponse}) => {
    return (
        <div className="flex flex-col space-y-3">
            <div className="text-lg font-semibold text-gray-800 dark:text-gray-200">
                Category Details
            </div>
            <div className="grid grid-cols-3 gap-y-2">
                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Name:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {category.name}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Description:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {category.description}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Status:
                </span>
                <span className="col-span-2">
                    <span className={`text-sm px-2 py-1 rounded-md ${category.isActive ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                        {category.isActive ? 'Active' : 'Inactive'}
                    </span>
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Created At:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {formatDateString(category.createdAt)}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Updated At:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {formatDateString(category.updatedAt)}
                </span>
            </div>
        </div>
    )
}

export default CategoryPopover;