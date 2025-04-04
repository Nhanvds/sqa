"use client";

import CreateOrUpdateCategory from "@/app/dashboard/category/_component/create-update-category";
import { CellActionPromotion } from "@/app/dashboard/promotion/component/cell-action";
import CreateOrUpdatePromotion from "@/app/dashboard/promotion/component/create-update-promotion";
import DebouncedInput from "@/components/debounce-input/debounce-input";
import PageContainer from "@/components/layout/page-container";
import { Spinner } from "@/components/spinner";
import { DataTable, DataTableColumnHeader } from "@/components/table/table-data";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { CustomAlert, CustomAlertProps } from "@/components/ui/CustomAlert";
import { formatDateString } from "@/constants/date";
import { getErrorMessage } from "@/constants/get-error";
import { useGetPromotionFilterQuery } from "@/redux/api/promotion-api";
import { PaginationParams } from "@/types/page";
import { PromotionResponse } from "@/types/promotion";
import { ColumnDef, ColumnSort } from "@tanstack/react-table";
import { AlertCircle } from "lucide-react";
import { useMemo, useState } from "react";


const PromotionTable = () => {
    const [pagination, setPagination] = useState<PaginationParams>({
        page: 0,
        size: 10,
        sortBy: "name",
        sortDirection: "asc",
        search: "",
    });

    const { data: allCategory, isFetching, error, refetch } = useGetPromotionFilterQuery(pagination);
    const [alert, setAlert] = useState<CustomAlertProps>({
        variant: "default",
        message: "",
    });
    const [showCreateModal, setShowCreateModal] = useState(false);
    const columns = useMemo<ColumnDef<PromotionResponse>[]>(() => [
        {
            accessorKey: "name",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Name" />,
        },
        {
            accessorKey: "discountPercentage",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Promotion (%)" />,
            cell: ({ row }) => (
                <div className="flex items-center justify-center w-full h-full">
                    {row.original.discountPercentage}%
                </div>
            ),
        },
        {
            accessorKey: "isActive",
            header: "Active",
            cell: ({ row }) => (
                <div>
                    {row.original.isActive ? "Active" : "Inactive"}
                </div>
            )
        },
        {
            accessorKey: "startDate",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Start Date" />,
            cell: ({ row }) => formatDateString(row.original.startDate),
        },
        {
            accessorKey: "endDate",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="End Date" />,
            cell: ({ row }) => formatDateString(row.original.endDate),
        },
        {
            accessorKey: "applyToAll",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Apply to All" />,
            cell: ({ row }) => (
                <div>
                    {row.original.applyToAll ? "Yes" : "No"}
                </div>
            ),
            enableSorting: true,
        },
        {
            accessorKey: "description",
            header: "Description",
        },
        {
            accessorKey: "createdAt",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Created" />,
            cell: ({ row }) => formatDateString(row.original.createdAt),
        },
        {
            id: 'actions',
            cell: ({ row }) => <CellActionPromotion data={row.original} refetch={refetch} setAlert={setAlert} />
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
                sortBy: "name",
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
                onClose={() => setAlert({ ...alert, show: false })}
            />
        )
    }

    return (
        <PageContainer scrollable>
            <CustomAlert
                {...alert}
                onClose={() => setAlert({ ...alert, show: false })}
            />
            <CreateOrUpdatePromotion
                isOpen={showCreateModal}
                onClose={() => setShowCreateModal(false)}
                setAlert={setAlert}
                refetch={refetch}
            />
            <div className="w-full px-4">
                <div className="flex items-center py-4 justify-between">
                    <DebouncedInput
                        placeholder="Filter name..."
                        value={pagination.search}
                        onChange={(value) =>
                            setPagination({
                                ...pagination,
                                search: value,
                            })
                        }
                        className="max-w-sm"

                    />
                    <Button size="lg" onClick={() => setShowCreateModal(true)}>
                        Add Category
                    </Button>
                </div>
                <div>
                    {isFetching ? <Spinner size="large" className="mt-10" /> : (
                        <DataTable
                            columns={columns}
                            data={allCategory?.result?.content ?? []}
                            size={pagination.size}
                            page={pagination.page}
                            setSize={(size) => setPagination((prev) => ({ ...prev, size }))}
                            setPage={(page) => setPagination((prev) => ({ ...prev, page }))}
                            total={allCategory?.result?.totalElements ?? 0}
                            onChangeSorting={handleSortingChange}
                        />
                    )}

                </div>
            </div>
        </PageContainer>
    );
};

export default PromotionTable;