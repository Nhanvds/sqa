'use client';

import CategoryPopover from '@/app/dashboard/product/component/category';
import { CellActionProduct } from '@/app/dashboard/product/component/cell-action';
import CreateOrUpdateProduct from '@/app/dashboard/product/component/create-update-product';
import PromotionDetail from '@/app/dashboard/product/component/promotion';
import DebouncedInput from '@/components/debounce-input/debounce-input';
import PageContainer from '@/components/layout/page-container';
import CustomSelect from '@/components/Select/select';
import { Spinner } from '@/components/spinner';
import { DataTable, DataTableColumnHeader } from '@/components/table/table-data';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { CustomAlert, CustomAlertProps } from '@/components/ui/CustomAlert';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { formatDateString } from '@/constants/date';
import { getErrorMessage } from '@/constants/get-error';
import { useGetAllCategoriesQuery } from '@/redux/api/category-api';
import { useGetProductFilterQuery } from '@/redux/api/product-api';
import { useLazyGetProductInventoryByProductIdsQuery } from '@/redux/api/product-inventory-api';
import { useGetAllPromotionQuery } from '@/redux/api/promotion-api';
import { StatusEnum } from '@/types/enums';
import { PaginationParams } from '@/types/page';
import { ProductResponse } from '@/types/product';
import { ProductInventoryResponse } from '@/types/product-inventory';
import { ColumnDef, ColumnSort } from '@tanstack/react-table';
import clsx from 'clsx';
import { AlertCircle } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

const ProductTable = () => {
    const [pagination, setPagination] = useState<PaginationParams>({
        page: 0,
        size: 10,
        sortBy: "",
        sortDirection: undefined,
        search: "",
    });

    const { data: allProduct, isFetching, error, refetch } = useGetProductFilterQuery(pagination);
    const [getProductInventoryByProductIds, { data: productInventory, isFetching: isFetchingProductInventory }] = useLazyGetProductInventoryByProductIdsQuery();
    const [mapProductInventory, setMapProductInventory] = useState<Map<string, ProductInventoryResponse>>(new Map());

    useEffect(() => {
        if (allProduct?.result?.content) {
            const productIds = allProduct.result.content.map(product => product.id);
            void getProductInventoryByProductIds({ productIds });
        }
    }, [allProduct]);

    useEffect(() => {
        if (productInventory?.result) {
            const map = new Map<string, ProductInventoryResponse>();
            productInventory.result.forEach(pi => {
                if (!map.has(pi.idProduct)) {
                    map.set(pi.idProduct, pi);
                }
            });
            setMapProductInventory(map);
        }
    }, [productInventory]);


    const { data: allCategory } = useGetAllCategoriesQuery();
    const { data: allPromotion } = useGetAllPromotionQuery();
    const [alert, setAlert] = useState<CustomAlertProps>({
        variant: "default",
        message: "",
    });
    const [showCreateModal, setShowCreateModal] = useState(false);
    const columns = useMemo<ColumnDef<ProductResponse>[]>(() => [
        {
            accessorKey: "name",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Name" />,
        },
        {
            accessorKey: "price",
            enableSorting: true,
            header: ({ column }) => <DataTableColumnHeader column={column} title="Price (đ)" />,
            cell: ({ row }) => <div>{row.original.price.toFixed(2)}</div>,
        },
        {
            accessorKey: "status",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Status" />,
            cell: ({ row }) => <div>{row.original.status === StatusEnum.ACTIVE ? "Active" : "Inactive"}</div>
        },
        {
            accessorKey: "categoryResponse.name",
            header: "Category",
            cell: ({ row }) => {
                const category = row.original.categoryResponse;
                return (
                    <Popover>
                        <PopoverTrigger asChild>
                            <Button variant="link">{category.name}</Button>
                        </PopoverTrigger>
                        <PopoverContent className="w-80 p-4 border rounded-md shadow-lg bg-white dark:bg-gray-800">
                            <div>
                                <CategoryPopover category={category} />
                            </div>
                        </PopoverContent>
                    </Popover>
                );
            }
        },
        {
            accessorKey: "promotions",
            header: "Promotions",
            cell: ({ row }) => {
                const promotions = row.original.promotions;
                const promotionApply = row.original.promotionResponse;
                return (
                    <div className="flex flex-col">
                        {promotions.map((promotion) => (
                            <Popover key={promotion.id}>
                                <PopoverTrigger asChild>
                                    <Button
                                        variant="link"
                                        className={clsx(
                                            promotion.id === promotionApply?.id ? "text-red-500 underline font-bold" : "text-primary"
                                        )}
                                    >
                                        {promotion.name}
                                    </Button>
                                </PopoverTrigger>
                                <PopoverContent>
                                    <PromotionDetail promotion={promotion} />
                                </PopoverContent>
                            </Popover>
                        ))}
                    </div>
                );
            }
        },
        {
            accessorKey: "rating",
            header: ({ column }) => <DataTableColumnHeader column={column} title="Rating" />,
            cell: ({ row }) => <div>{row.original.rating}</div>
        },
        {
            accessorKey: "size",
            header: "Size",
            cell: ({ row }) => {
                if (isFetchingProductInventory) {
                    return <Spinner size="small" />;
                }
                const products = productInventory?.result?.filter((pi) => pi.idProduct === row.original.id);
                if (!products || products.length === 0) {
                    return (
                        <div>Not available</div>
                    )
                }

                return (
                    <CustomSelect
                        options={products.map((pi) => ({
                            label: pi.size.name.toLocaleUpperCase(),
                            value: pi.size.id,
                        }))}
                        value={mapProductInventory.get(row.original.id)?.size.id ?? ""}
                        onChange={(value) => {
                            const productInventory = mapProductInventory.get(row.original.id);
                            if (productInventory) {
                                setMapProductInventory((prev) => {
                                    const newMap = new Map(prev);
                                    newMap.set(row.original.id, products.find((pi) => pi.size.id === value) ?? productInventory);
                                    return newMap;
                                });
                            }
                        }}
                        placeholder='Select size'
                    />);
            }
        },
        {
            accessorKey: "quantity",
            header: "Quantity",
            cell: ({ row }) => {
                if (isFetchingProductInventory) {
                    return <Spinner size="small" />;
                }
                const productInventory = mapProductInventory.get(row.original.id);
                if (!productInventory) {
                    return (
                        <></>
                    )
                }
                return (
                    <div>{productInventory.quantity}</div>
                );
            }
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
            cell: ({ row }) => <CellActionProduct 
                                    data={row.original}
                                    refetch={() => {
                                        refetch().then(() => {
                                            void getProductInventoryByProductIds({ productIds: allProduct?.result?.content.map(product => product.id) ?? [] });
                                        });
                                    }}
                                    setAlert={setAlert}
                                    categories={allCategory?.result ?? []}
                                    promotions={allPromotion?.result ?? []}
                                    productInventory={productInventory?.result?.filter((pi) => pi.idProduct === row.original.id) ?? []} 
                                />,
        }
    ], [allCategory, allPromotion, refetch, mapProductInventory, isFetchingProductInventory, productInventory]);

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
            <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Error</AlertTitle>
                <AlertDescription>
                    {getErrorMessage(error)}
                </AlertDescription>
            </Alert>
        )
    }

    return (
        <PageContainer scrollable>
            <CustomAlert {...alert} onClose={() => setAlert({ variant: "default", message: "" })} />
            <CreateOrUpdateProduct
                isOpen={showCreateModal}
                onClose={() => setShowCreateModal(false)}
                refetch={() => {
                    refetch().then(() => {
                        void getProductInventoryByProductIds({ productIds: allProduct?.result?.content.map(product => product.id) ?? [] });
                    });
                }}
                categories={allCategory?.result ?? []}
                promotions={allPromotion?.result ?? []}
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
                        Add Product
                    </Button>
                </div>
                <div>
                        <DataTable
                            columns={columns}
                            data={allProduct?.result?.content ?? []}
                            size={pagination.size}
                            page={pagination.page}
                            setSize={(size) => setPagination((prev) => ({ ...prev, size }))}
                            setPage={(page) => setPagination((prev) => ({ ...prev, page }))}
                            total={allProduct?.result?.totalElements ?? 0}
                            onChangeSorting={handleSortingChange}
                            loading={isFetching}
                            pathKey='product'
                        />

                </div>
            </div>
        </PageContainer>
    );
};

export default ProductTable;
