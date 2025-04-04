
"use client";

import DebouncedInput from "@/components/debounce-input/debounce-input";
import PageContainer from "@/components/layout/page-container";
import { Spinner } from "@/components/spinner";
import { DataTable, DataTableColumnHeader } from "@/components/table/table-data";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { formatDateString } from "@/constants/date";
import { getErrorMessage } from "@/constants/get-error";
import { PaginationParamsExtra } from "@/types/page";
import { OrderResponse } from "@/types/order";
import { ColumnDef, ColumnSort } from "@tanstack/react-table";
import { AlertCircle } from "lucide-react";
import { useMemo, useState } from "react";
import { OrderStatusEnum } from "@/types/enums";
import { useGetOrdersForAdminQuery } from "@/redux/api/order-api";
import { CellActionOrder } from "@/app/dashboard/order/component/cell-action";
import CreateOrUpdateOrder from "@/app/dashboard/order/component/create-update-order";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import UserDetailsPopover from "@/app/dashboard/order/component/user-detail";
import OrderItemDetailsPopover from "@/app/dashboard/order/component/order-item-detail";
import { formatPrice } from "@/constants/format";
import { CustomAlert, CustomAlertProps } from "@/components/ui/CustomAlert";

const OrderTable = () => {
    const [pagination, setPagination] = useState<PaginationParamsExtra>({
        page: 0,
        size: 10,
        productName: "",
        customerEmail: "",
        status: OrderStatusEnum.ALL, 
        sortBy: "user.email",
        sortDirection: "asc",
    });

    const { data: ordersData, isFetching, error, refetch } = useGetOrdersForAdminQuery(pagination);
    
    const [alert, setAlert] = useState<CustomAlertProps>({
        variant: "default",
        message: "",
    });
    // const [showCreateModal, setShowCreateModal] = useState(false);

    const columns = useMemo<ColumnDef<OrderResponse>[]>(() => [
        {
            accessorKey: "user.email",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Customer Email" />,
            cell: ({ row }) => {
                const user = row.original.user;
                return (
                    <Popover>
                        <PopoverTrigger asChild>
                            <Button variant="link">{user.email}</Button>
                        </PopoverTrigger>
                        <PopoverContent>
                            <UserDetailsPopover user={user} />
                        </PopoverContent>
                    </Popover>
                );
            },
        },
        {
            accessorKey: "orderItems",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Product Name" />,
            cell: ({ row }) => {
                const orderItems = row.original.orderItems;
                return (
                    <div className="flex flex-col">
                        {orderItems.map((item) => (
                            <Popover key={item.id}>
                                <PopoverTrigger asChild>
                                    <Button variant="link">{item.productResponse.name}</Button>
                                </PopoverTrigger>
                                <PopoverContent>
                                    <OrderItemDetailsPopover orderItem={item} />
                                </PopoverContent>
                            </Popover>
                        ))}
                    </div>
                );
            },
        },
        {
            accessorKey: "status",
            header: "Order Status",
            cell: ({ row }) => (
                <div>
                    {row.original.status}
                </div>
            ),
            enableSorting: true,
        },
        {
            accessorKey: "createdAt",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Order Date" />,
            cell: ({ row }) => formatDateString(row.original.createdAt),
        },
        {
            accessorKey: "totalPriceBeforeDiscount",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Total Price Before Discount" />,
            cell: ({ row }) => formatPrice(row.original.totalPriceBeforeDiscount),
        },
        {
            accessorKey: "totalPriceAfterDiscount",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Total Price After Discount" />,
            cell: ({ row }) => formatPrice(row.original.totalPriceAfterDiscount),
        },
        {
            id: 'actions',
            cell: ({ row }) => <CellActionOrder data={row.original} refetch={refetch} setAlert={setAlert} />
        }
    ], []);

    const handleSortingChange = (newSorting?: ColumnSort) => {
        if (newSorting) {
            setPagination((prev) => ({
                ...prev,
                sortBy: newSorting.id,
                sortDirection: newSorting.desc ? "desc" : "asc",
            }));
        } else {
            setPagination((prev) => ({
                ...prev,
                sortBy: "customerName",
                sortDirection: "asc",
            }));
        }
    };

    if (error) {
        return (
            <CustomAlert
                show={true}
                variant="destructive"
                message={getErrorMessage(error)}
            />
        );
    }

    return (
        <PageContainer scrollable>
            <CustomAlert
                {...alert}
                onClose={() => setAlert({ variant: "default", message: "" })}
            />

            {/* <CreateOrUpdateOrder
                isOpen={showCreateModal}
                onClose={() => setShowCreateModal(false)}
                setMessageError={setMessageError}
                refetch={refetch}
            /> */}
            <div className="w-full px-4">
                <div className="flex items-center py-4 justify-between">
                    <DebouncedInput
                        placeholder="Search by customer email..."
                        value={pagination.customerEmail}
                        onChange={(value) =>
                            setPagination({
                                ...pagination,
                                customerEmail: value,
                            })
                        }
                        className="max-w-sm"
                    />
                    <DebouncedInput
                        placeholder="Search by product name..."
                        value={pagination.productName}
                        onChange={(value) =>
                            setPagination({
                                ...pagination,
                                productName: value,
                            })
                        }
                        className="max-w-sm"
                    />
                    <div className="flex space-x-4">
                        <select
                            value={pagination.status}
                            onChange={(e) => setPagination({ ...pagination, status: e.target.value as OrderStatusEnum })}
                            className="border rounded-md p-2"
                        >
                            <option value={OrderStatusEnum.ALL}>All</option>
                            <option value={OrderStatusEnum.PENDING}>Pending</option>
                            <option value={OrderStatusEnum.CONFIRMED}>Confirmed</option>
                            <option value={OrderStatusEnum.COMPLETED}>Completed</option>
                            <option value={OrderStatusEnum.DELIVERED}>Delivered</option>
                            <option value={OrderStatusEnum.CANCELLED}>Cancelled</option>
                        </select>
                        {/* <Button size="lg" onClick={() => setShowCreateModal(true)}>
                            Add Order
                        </Button> */}
                    </div>
                </div>
                <div>
                    {isFetching ? <Spinner size="large" className="mt-10" /> : (
                        <DataTable
                            columns={columns}
                            data={ordersData?.result?.content ?? []}
                            size={pagination.size}
                            page={pagination.page}
                            setSize={(size) => setPagination((prev) => ({ ...prev, size }))}
                            setPage={(page) => setPagination((prev) => ({ ...prev, page }))}
                            total={ordersData?.result?.totalElements ?? 0}
                            onChangeSorting={handleSortingChange}
                        />
                    )}
                </div>
            </div>
        </PageContainer>
    );
};

export default OrderTable;
