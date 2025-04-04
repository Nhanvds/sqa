"use client";

import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { CategoryResponse } from "@/types/category";
import { ProductResponse } from "@/types/product";
import { PromotionResponse } from "@/types/promotion";
import { useForm } from "react-hook-form";
import { useState } from "react";
import { StatusEnum } from "@/types/enums";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ProductInventoryResponse } from "@/types/product-inventory";
import CreateUpdateProductTab from "@/app/dashboard/product/component/create-update-product-tab";
import CreateUpdateProductInventoryTab from "@/app/dashboard/product/component/create-update-product-inventory-tab";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { AlertCircle } from "lucide-react";
import { Tooltip, TooltipContent, TooltipTrigger } from "@/components/ui/tooltip";
import { CustomAlert, CustomAlertProps } from "@/components/ui/CustomAlert";

enum ETab {
    PRODUCT = "product",
    PRODUCT_INVENTORY = "product-inventory",
}

interface CreateOrUpdateProductProps {
    isOpen: boolean;
    onClose: () => void;
    product?: ProductResponse;
    refetch: () => void;
    categories: CategoryResponse[];
    promotions: PromotionResponse[];
    productInventory?: ProductInventoryResponse[];
}

const CreateOrUpdateProduct = ({
    isOpen,
    onClose,
    product,
    refetch,
    categories,
    promotions,
    productInventory,
}: CreateOrUpdateProductProps) => {
    const [productId, setProductId] = useState(product?.id ?? "");
    const [activeTab, setActiveTab] = useState<ETab>(ETab.PRODUCT);

    const [alert, setAlert] = useState<CustomAlertProps>({
        variant: "default",
        message: "",
    });

    const handleTabChange = (value: string) => {
        if (productId) {
            setActiveTab(value as ETab);
        }
    }

    return (
        <Dialog open={isOpen} onOpenChange={onClose} modal>
            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle>{product ? "Edit Product" : "Add Product"}</DialogTitle>
                    <DialogDescription>
                        {product
                            ? `Update the ${activeTab === ETab.PRODUCT ? "product" : "product inventory"} details below.`
                            : `Fill in the details to create a new ${activeTab === ETab.PRODUCT ? "product" : "product inventory"}.`}
                    </DialogDescription>
                </DialogHeader>

                <Tabs value={activeTab} onValueChange={handleTabChange} className="w-full">
                    <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value={ETab.PRODUCT}>Product</TabsTrigger>
                        {!productId ? (
                            <Tooltip>
                                <TooltipTrigger asChild>
                                    <TabsTrigger value={ETab.PRODUCT_INVENTORY}>
                                        Product Inventory
                                    </TabsTrigger>
                                </TooltipTrigger>
                                <TooltipContent>
                                    <p>You need to create a product first!</p>
                                </TooltipContent>
                            </Tooltip>
                        ) : (
                            <TabsTrigger value={ETab.PRODUCT_INVENTORY}>
                                Product Inventory
                            </TabsTrigger>
                        )}
                    </TabsList>

                    <CustomAlert
                        {...alert}
                        onClose={() => setAlert({ variant: "default", message: "" })}
                    />

                    <TabsContent value={ETab.PRODUCT}>
                        <CreateUpdateProductTab
                            product={product}
                            categories={categories}
                            promotions={promotions}
                            refetch={refetch}
                            setAlert={setAlert}
                            onClose={onClose}
                            setProductId={setProductId}
                        />
                    </TabsContent>

                    <TabsContent value={ETab.PRODUCT_INVENTORY}>
                        <CreateUpdateProductInventoryTab
                            productInventory={productInventory ?? []}
                            productId={productId}
                            refetch={refetch}
                            onClose={onClose}
                            setAlert={setAlert}
                        />
                    </TabsContent>
                </Tabs>

            </DialogContent>
        </Dialog>
    );
};


export default CreateOrUpdateProduct;
