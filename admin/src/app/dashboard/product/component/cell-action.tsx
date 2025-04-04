'use client';
import CreateOrUpdateProduct from '@/app/dashboard/product/component/create-update-product';
import { AlertModal } from '@/components/modal/alert-modal';
import { Button } from '@/components/ui/button';
import { CustomAlertProps } from '@/components/ui/CustomAlert';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import { useDeleteProductMutation } from '@/redux/api/product-api';
import { CategoryResponse } from '@/types/category';
import { ProductResponse } from '@/types/product';
import { ProductInventoryResponse } from '@/types/product-inventory';
import { PromotionResponse } from '@/types/promotion';
import { Edit, MoreHorizontal, Trash } from 'lucide-react';
import { useState } from 'react';

interface CellActionProps {
    data: ProductResponse;
    refetch: () => void;
    setAlert: (alert: CustomAlertProps) => void;
    categories: CategoryResponse[];
    promotions: PromotionResponse[];
    productInventory: ProductInventoryResponse[];
}

export const CellActionProduct: React.FC<CellActionProps> = ({ data, refetch, setAlert, categories, promotions, productInventory }) => {
    const [deleteProduct, { isLoading: isDeleting }] = useDeleteProductMutation();
    
    const [showUpdateModal, setShowUpdateModal] = useState(false);
    const [open, setOpen] = useState(false);

    const onConfirm = () => {
        deleteProduct(data.id).unwrap()
            .then(() => {
                setAlert({
                    show: true,
                    message: `Product ${data.name} has been deleted`,
                    variant: "success"
                });
                setOpen(false);
                refetch();
            }).catch((error) => {
                setAlert({
                    show: true,
                    message: error.message,
                    variant: "destructive"
                });
                setOpen(false);
            });
    };

    return (
        <>
            <AlertModal
                isOpen={open}
                onClose={() => setOpen(false)}
                onConfirm={onConfirm}
                loading={isDeleting}
                title={`Delete ${data.name}`}
                description='Are you sure you want to delete this product?'
            />
            <CreateOrUpdateProduct
                isOpen={showUpdateModal}
                onClose={() => setShowUpdateModal(false)}
                product={data}
                refetch={refetch}
                categories={categories}
                promotions={promotions}
                productInventory={productInventory}
            />
            <DropdownMenu modal={false}>
                <DropdownMenuTrigger asChild>
                    <Button variant="ghost" className="h-8 w-8 p-0">
                        <span className="sr-only">Open menu</span>
                        <MoreHorizontal className="h-4 w-4" />
                    </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                    <DropdownMenuLabel>Actions</DropdownMenuLabel>

                    <DropdownMenuItem
                        onClick={() => setShowUpdateModal(true)}
                    >
                        <Edit className="mr-2 h-4 w-4" /> Update
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => setOpen(true)}>
                        <Trash className="mr-2 h-4 w-4" /> Delete
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </>
    );
};