"use client";

import { useGetAllUserQuery } from "@/redux/api/user-api";
import { PaginationParams } from "@/types/page";
import { useRouter } from "next/router";
import { useMemo, useState } from "react";
import { User } from "@/types/user-info";
import { LoyaltyTierEnum, StatusEnum } from "@/types/enums";
import { Badge } from "@/components/ui/badge";
import {
    ColumnDef,
    ColumnSort,
} from "@tanstack/react-table"
import { Button } from "@/components/ui/button";
import { DataTable, DataTableColumnHeader } from "@/components/table/table-data";
import PageContainer from "@/components/layout/page-container";
import DebouncedInput from "@/components/debounce-input/debounce-input";
import { Spinner } from "@/components/spinner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircle } from "lucide-react";
import { getErrorMessage } from "@/constants/get-error";
import { formatDateString } from "@/constants/date";
import { CellActionUser } from "@/app/dashboard/user/component/cell-action";
import CreateOrUpdateUser from "@/app/dashboard/user/component/create-update-user";
import { CustomAlert, CustomAlertProps } from "@/components/ui/CustomAlert";

const UserPage = () => {
    // const router = useRouter();
    // const { query } = router;

    const [pagination, setPagination] = useState<PaginationParams>({
        // page: query.page ? parseInt(query.page as string) : 0,
        // size: query.size ? parseInt(query.size as string) : 10,
        // sortBy: (query.sortBy as string) ?? "name",
        // sortDirection: "asc",
        // search: (query.name as string) ?? "",
        page: 0,
        size: 10,
        sortBy: "name",
        sortDirection: "asc",
        search: "",
    });
    const { data: allUser, isFetching, error, refetch } = useGetAllUserQuery(pagination);
    const [alert, setAlert] = useState<CustomAlertProps>({
        variant: "default",
        message: "",
    });
    const [showCreateModal, setShowCreateModal] = useState(false);

    const columns = useMemo<ColumnDef<User>[]>(() => [
        {
            accessorKey: "email",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Email" />,
        },
        {
            accessorKey: "name",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Name" />,
        },
        {
            accessorKey: "status",
            header: "Active",
            cell: ({ row }) => (
                <div>
                    {row.original.status === StatusEnum.ACTIVE ? "Active" : "Inactive"}
                </div>
            )
        },
        {
            accessorKey: "loyaltyTier",
            header: "Loyalty Tier",
            cell: ({ row }) => (
                <div>
                    {row.original.loyaltyTier ?? ""}
                </div>
            )
        },
        {
            accessorKey: "roles",
            header: "Roles",
            cell: ({ row }) => (
                <div>
                    {Array.from(row.original.roles).map(role => (
                        <Badge key={role}>{role}</Badge>
                    ))}
                </div>
            )
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
                    cell: ({ row }) => <CellActionUser data={row.original} refetch={refetch} setAlert={setAlert} />
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
                show
                message={getErrorMessage(error)}
                variant="destructive"
            />
        )
    }

    return (
        <PageContainer scrollable>
            <CustomAlert
                {...alert}
                onClose={() => setAlert({ message: "", show: false, variant: "default" })}
            />
            <CreateOrUpdateUser
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
                        Add User
                    </Button>
                </div>
                <div>
                    {isFetching ? <Spinner size="large" className="mt-10" /> : (
                        <DataTable
                            columns={columns}
                            data={allUser?.result?.content ?? []}
                            size={pagination.size}
                            page={pagination.page}
                            setSize={(size) => setPagination((prev) => ({ ...prev, size }))}
                            setPage={(page) => setPagination((prev) => ({ ...prev, page }))}
                            total={allUser?.result?.totalElements ?? 0}
                            onChangeSorting={handleSortingChange}
                        />
                    )}

                </div>
            </div>
        </PageContainer>
    );
}

export default UserPage;