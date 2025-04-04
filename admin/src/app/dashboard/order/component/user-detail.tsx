import { formatDateString } from "@/constants/date";
import { StatusEnum } from "@/types/enums";
import { User } from "@/types/user-info";

const UserDetailsPopover = ({ user }: { user: User }) => {
    return (
        <div className="flex flex-col space-y-3">
            <div className="text-lg font-semibold text-gray-800 dark:text-gray-200">
                User Details
            </div>
            <div className="grid grid-cols-3 gap-y-2">
                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Name:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {user.name}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Email:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {user.email}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Phone Numbers:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {user.phoneNumbers?.map(phone => <div key={phone.id}>{phone.phoneNumber}</div>)}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Loyalty Tier:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {user.loyaltyTier}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Status:
                </span>
                <span className="col-span-2">
                    <span
                        className={`text-sm px-2 py-1 rounded-md font-medium ${user.status === StatusEnum.ACTIVE
                                ? 'bg-green-50 text-green-600'
                                : user.status === StatusEnum.INACTIVE
                                    ? 'bg-yellow-50 text-yellow-600'
                                    : 'bg-red-50 text-red-600'
                            }`}
                    >
                        {user.status === StatusEnum.ACTIVE
                            ? 'Active'
                            : user.status === StatusEnum.INACTIVE
                                ? 'Inactive'
                                : 'Deleted'}
                    </span>
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Created At:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {formatDateString(user.createdAt)}
                </span>

                <span className="col-span-1 text-sm font-medium text-gray-600 dark:text-gray-400">
                    Updated At:
                </span>
                <span className="col-span-2 text-sm text-gray-800 dark:text-gray-200">
                    {formatDateString(user.updatedAt)}
                </span>
            </div>
        </div>
    );
};

export default UserDetailsPopover;
