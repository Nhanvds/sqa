"use client";

import { CellAction } from "@/app/dashboard/category/_component/cell-action";
import CreateOrUpdateCategory from "@/app/dashboard/category/_component/create-update-category";
import DebouncedInput from "@/components/debounce-input/debounce-input";
import PageContainer from "@/components/layout/page-container";
import { Spinner } from "@/components/spinner";
import { DataTable, DataTableColumnHeader } from "@/components/table/table-data";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { CustomAlert, CustomAlertProps } from "@/components/ui/CustomAlert";
import { formatDateString } from "@/constants/date";
import { getErrorMessage } from "@/constants/get-error";
import { useGetAllCategoryQuery } from "@/redux/api/category-api";
import { CategoryResponse } from "@/types/category";
import { StatusEnum } from "@/types/enums";
import { PaginationParams } from "@/types/page";
import { ColumnDef, ColumnSort } from "@tanstack/react-table";
import { AlertCircle } from "lucide-react";
import { useMemo, useState } from "react";


const CategoryPage = () => {
    const [pagination, setPagination] = useState<PaginationParams>({
        page: 0,
        size: 10,
        sortBy: "name",
        sortDirection: "asc",
        search: "",
    });

    const { data: allCategory, isFetching, error, refetch } = useGetAllCategoryQuery(pagination);
    const [alert, setAlert] = useState<CustomAlertProps>({
        variant: "default",
        message: "",
    });
    const [showCreateModal, setShowCreateModal] = useState(false);
    const columns = useMemo<ColumnDef<CategoryResponse>[]>(() => [
        {
            accessorKey: "name",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Name" />,
        },
        {
            accessorKey: "status",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Status" />,
            cell: ({ row }) => (
                <div>
                    {row.original.status === StatusEnum.ACTIVE ? "Active" : "Inactive"}
                </div>
            )
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
            accessorKey: "updatedAt",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Updated" />,
            cell: ({ row }) => formatDateString(row.original.updatedAt),
        },
        {
            id: 'actions',
            cell: ({ row }) => <CellAction data={row.original} refetch={refetch} setAlert={setAlert} />
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

    return (
        <PageContainer scrollable>
            <CustomAlert {...alert} onClose={() => setAlert({ message: "", variant: "default" })} />
            <CreateOrUpdateCategory
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

export default CategoryPage;