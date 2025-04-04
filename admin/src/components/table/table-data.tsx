"use client"

import {
    Column,
    ColumnDef,
    ColumnSort,
    flexRender,
    getCoreRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    SortingState,
    Updater,
    useReactTable,
} from "@tanstack/react-table"

import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { useEffect, useMemo, useState } from "react"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ArrowUpDown, ChevronDown, ChevronLeftIcon, ChevronRightIcon } from "lucide-react"
import { DoubleArrowLeftIcon, DoubleArrowRightIcon } from "@radix-ui/react-icons"
import { getTableSavedPageSize, getTableSavedSortingState } from "@/components/table/helper"
import { Spinner } from "@/components/spinner"

interface DataTableProps<TData, TValue> {
    columns: ColumnDef<TData, TValue>[]
    data: TData[],
    onChangeSorting?: (sorting?: ColumnSort) => void
    size?: number
    page?: number
    setSize: (size: number) => void
    setPage: (page: number) => void
    total: number
    pathKey?: string
    keepSortState?: boolean
    loading?: boolean
}

const optionSizes = [10, 20, 30, 50]

interface DataTableColumnHeaderProps<TData, TValue> {
    column: Column<TData, TValue>;
    title: string;
}

export const DataTableColumnHeader = <TData, TValue>({ column, title }: DataTableColumnHeaderProps<TData, TValue>) => {
    return (
        <Button
            variant="ghost"
            onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
            className="gap-1 px-0"
        >
            {title}
            <ArrowUpDown size="16" />
        </Button>
    );
};

export function DataTable<TData, TValue>({
    columns,
    data,
    onChangeSorting,
    size = 10,
    page = 0,
    setSize,
    setPage,
    total,
    pathKey = "",
    keepSortState = true,
    loading = false,
}: DataTableProps<TData, TValue>) {
    const [{ pageIndex, pageSize }, setPagination] = useState(() =>
        getTableSavedPageSize(pathKey, page, size)
    );

    useEffect(() => {
        if (setPage) {
            setPage(pageIndex);
        }
    }, [pageIndex]);

    useEffect(() => {
        if (setSize) {
            setSize(pageSize);
        }
    }, [pageSize]);

    const savedSortState = useMemo(
        () => (keepSortState ? getTableSavedSortingState(pathKey) : []),
        [pathKey, keepSortState]
    );

    const [sorting, setSorting] = useState<SortingState>(savedSortState);
    useEffect(() => {
        if (sorting.length > 0) {
            onChangeSorting?.(sorting[0]);
        } else {
            onChangeSorting?.(undefined);
        }
    }, [sorting]);

    const table = useReactTable({
        data,
        columns,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        state: {
            sorting,
        },
        onSortingChange: setSorting,
        pageCount: Math.ceil(total / size),
        initialState: {
            pagination: {
                pageSize: size,
                pageIndex: page,
            },
        },
    });

    useEffect(() => {
        
        console.log(table.getRowModel().rows);
    }, [table])

    return (
        <>
            {loading ? (
                <Spinner size="large" className="mt-10" />) : (
                <div className="rounded-md border">
                    <Table>
                        <TableHeader>
                            {table.getHeaderGroups().map((headerGroup) => (
                                <TableRow key={headerGroup.id}>
                                    {headerGroup.headers.map((header) => {
                                        return (
                                            <TableHead key={header.id}>
                                                {header.isPlaceholder
                                                    ? null
                                                    : flexRender(
                                                        header.column.columnDef.header,
                                                        header.getContext()
                                                    )}
                                            </TableHead>
                                        )
                                    })}
                                </TableRow>
                            ))}
                        </TableHeader>
                        <TableBody>
                            {table.getRowModel().rows?.length ? (
                                table.getRowModel().rows.map((row) => (
                                    <TableRow
                                        key={row.id}
                                        data-state={row.getIsSelected() && "selected"}
                                    >
                                        {row.getVisibleCells().map((cell) => (
                                            <TableCell key={cell.id}>
                                                {flexRender(cell.column.columnDef.cell, cell.getContext())}
                                            </TableCell>
                                        ))}
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={columns.length} className="h-24 text-center">
                                        No results.
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </div>
            )}

            <div className="flex items-center justify-between py-4">
                <div className="flex items-center space-x-4">
                    <div className="text-sm text-muted-foreground mr-2">
                        {page * size + 1} to {page * size + data.length} of {total}
                    </div>
                </div>
                <div className="flex items-center space-x-4">
                    <DropdownMenu>
                        <span className="text-sm text-muted-foreground">Rows per page: </span>
                        <DropdownMenuTrigger asChild>
                            <Button variant="outline" className="gap-4" size="sm">
                                {size}
                                <ChevronDown />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="start">
                            {optionSizes.map((sizeOption) => (
                                <DropdownMenuItem
                                    key={sizeOption}
                                    onClick={() => setSize(sizeOption)}
                                >
                                    {sizeOption}
                                </DropdownMenuItem>
                            ))}
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>

                <div className="flex items-center justify-between gap-2 sm:justify-end">
                    <div className="flex w-[150px] items-center justify-center text-sm font-medium">
                        {total > 0 ? (
                            <>
                                Page {page + 1} of {table.getPageCount()}
                            </>
                        ) : (
                            'No pages'
                        )}
                    </div>
                    <div className="flex items-center space-x-2">
                        <Button
                            aria-label="Go to first page"
                            variant="outline"
                            className="hidden h-8 w-8 p-0 lg:flex"
                            onClick={() => setPage(0)}
                            disabled={page === 0}
                        >
                            <DoubleArrowLeftIcon className="h-4 w-4" aria-hidden="true" />
                        </Button>
                        <Button
                            aria-label="Go to previous page"
                            variant="outline"
                            className="h-8 w-8 p-0"
                            onClick={() => setPage(page - 1)}
                            disabled={page < 1}
                        >
                            <ChevronLeftIcon className="h-4 w-4" aria-hidden="true" />
                        </Button>
                        <Button
                            aria-label="Go to next page"
                            variant="outline"
                            className="h-8 w-8 p-0"
                            onClick={() => setPage(page + 1)}
                            disabled={page >= table.getPageCount() - 1}
                        >
                            <ChevronRightIcon className="h-4 w-4" aria-hidden="true" />
                        </Button>
                        <Button
                            aria-label="Go to last page"
                            variant="outline"
                            className="hidden h-8 w-8 p-0 lg:flex"
                            onClick={() => setPage(table.getPageCount() - 1)}
                            disabled={page >= table.getPageCount() - 1}
                        >
                            <DoubleArrowRightIcon className="h-4 w-4" aria-hidden="true" />
                        </Button>
                    </div>
                </div>
            </div>
        </>
    )
}
