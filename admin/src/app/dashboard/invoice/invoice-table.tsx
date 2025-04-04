"use client";

import { useState, useMemo } from "react";
import { useGetInvoiceQuery } from "@/redux/api/invoice-api";
import { Button } from "@/components/ui/button";
import { DataTable, DataTableColumnHeader } from "@/components/table/table-data";
import { Spinner } from "@/components/spinner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { formatDateString } from "@/constants/date";
import { PaginationParams } from "@/types/page";
import { InvoiceResponse } from "@/types/invoice";
import { ColumnDef, ColumnSort } from "@tanstack/react-table";
import DebouncedInput from "@/components/debounce-input/debounce-input";
import { getErrorMessage } from "@/constants/get-error";
import { AlertCircle } from "lucide-react";
import { InvoiceStatusEnum } from "@/types/enums";
import { formatPrice } from "@/constants/format";
import { CustomAlert } from "@/components/ui/CustomAlert";

const InvoiceTable = () => {
    const [pagination, setPagination] = useState<PaginationParams>({
        page: 0,
        size: 10,
        sortBy: "invoiceNumber",
        sortDirection: "asc",
        search: "",
    });

    const { data: allInvoices, isFetching, error, refetch } = useGetInvoiceQuery(pagination);

    const columns = useMemo<ColumnDef<InvoiceResponse>[]>(() => [
        {
            accessorKey: "invoiceNumber",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Invoice Number" />,
        },
        {
            accessorKey: "totalAmount",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Total Amount (VND)" />,
            cell: ({ row }) => formatPrice(row.original.totalAmount),
        },
        {
            accessorKey: "status",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Status" />,
            cell: ({ row }) => row.original.status,
        },
        {
            accessorKey: "payment.paymentMethod",
            header: "Payment Method",
            cell: ({ row }) => row.original.payment?.paymentMethod ? row.original.payment.paymentMethod : "TRANSFER",
        },
        {
            accessorKey: "payment.paymentStatus",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Payment Status" />,
            cell: ({ row }) => row.original.payment?.paymentStatus ? row.original.payment.paymentStatus : "PENDING",
        },
        {
            accessorKey: "createdAt",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Created At" />,
            cell: ({ row }) => formatDateString(row.original.createdAt),
        },
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
                sortBy: "invoiceNumber",
                sortDirection: "asc",
            }));
        }
    };

    if (error) {
        return (
            <CustomAlert
                variant="destructive"
                message={getErrorMessage(error)}
                onClose={() => refetch()}
            />
        );
    }

    return (
        <div>
            <div className="w-full px-4">
                <div className="flex items-center py-4 justify-between">
                    <DebouncedInput
                        placeholder="Filter by customer email..."
                        value={pagination.search}
                        onChange={(value) =>
                            setPagination({
                                ...pagination,
                                search: value,
                            })
                        }
                        className="max-w-sm"
                    />
                    {/* <Button size="lg" onClick={() => setShowCreateModal(true)}>
                        Add Invoice
                    </Button> */}
                </div>
                <div>
                    {isFetching ? (
                        <Spinner size="large" className="mt-10" />
                    ) : (
                        <DataTable
                            columns={columns}
                            data={allInvoices?.result?.content ?? []}
                            size={pagination.size}
                            page={pagination.page}
                            setSize={(size) => setPagination((prev) => ({ ...prev, size }))}
                            setPage={(page) => setPagination((prev) => ({ ...prev, page }))}
                            total={allInvoices?.result?.totalElements ?? 0}
                            onChangeSorting={handleSortingChange}
                        />
                    )}
                </div>
            </div>
        </div>
    );
};

export default InvoiceTable;
