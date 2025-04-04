import { Spinner } from "@/components/spinner";
import { Button } from "@/components/ui/button";
import { CustomAlertProps } from "@/components/ui/CustomAlert";
import { DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { getErrorMessage } from "@/constants/get-error";
import { useCreateProductInventoryMutation, useDeleteProductInventoryMutation, useUpdateProductInventoryMutation } from "@/redux/api/product-inventory-api";
import { useCreateSizeMutation, useGetAllSizeQuery } from "@/redux/api/size-api";
import { ProductInventoryRequest, ProductInventoryResponse } from "@/types/product-inventory";
import { DeleteIcon, TrashIcon } from "lucide-react";
import { useEffect, useState } from "react";

interface ProductInventoryTabProps {
    productInventory: ProductInventoryResponse[];
    productId: string;
    refetch: () => void;
    onClose: () => void;
    setAlert: (alert: CustomAlertProps) => void;
}

interface RowProductInventory {
    id?: string;
    sizeName: string;
    quantity: number;
}

interface ProductInventoryRequestWithId extends ProductInventoryRequest { id: string };

const CreateUpdateProductInventoryTab = ({ productInventory, productId, refetch, onClose, setAlert }: ProductInventoryTabProps) => {
    const [rows, setRows] = useState<RowProductInventory[]>(productInventory.map((inventory) => ({
        id: inventory.id,
        sizeName: inventory.size.name,
        quantity: inventory.quantity,
    })));

    const [createSize, { isLoading: isCreatingSize, error: createSizeError }] = useCreateSizeMutation();
    const { isLoading: isGettingSizes, refetch: refetchSizes } = useGetAllSizeQuery();
    const [createProductInventory, { isLoading: isCreatingProductInventory, error: createError }] = useCreateProductInventoryMutation();
    const [updateProductInventory, { isLoading: isUpdatingProductInventory, error: updateError }] = useUpdateProductInventoryMutation();
    const [deleteProductInventory, { isLoading: isDeletingProductInventory, error: deleteError }] = useDeleteProductInventoryMutation();

    useEffect(() => {
        if (createError || updateError || deleteError || createSizeError) {
            setAlert({
                show: true,
                message: (getErrorMessage(createError) || getErrorMessage(updateError) || getErrorMessage(deleteError) || getErrorMessage(createSizeError)) ?? "Error",
                variant: "destructive",
            });
        }
    }, [createError, updateError, deleteError, createSizeError]);

    const handleSave = async () => {
        const newSizes = rows.filter((row) => !productInventory.some((inventory) => inventory.size.name === row.sizeName));

        await Promise.all(newSizes.map((size) => createSize({ name: size.sizeName })));
        const allsSizesData = await refetchSizes().unwrap();
        const [newProductInventoryRow, updateProductInventoryRow] = rows.reduce((acc, row) => {
            if (row.id) {
                acc[1].push({ id: row.id, idProduct: productId, quantity: row?.quantity ?? 0, idSize: allsSizesData?.result?.find((size) => size.name.toLocaleUpperCase() === row.sizeName.toLocaleUpperCase())?.id ?? "" });
            }
            else {
                acc[0].push({ idProduct: productId, quantity: row?.quantity ?? 0, idSize: allsSizesData?.result?.find((size) => size.name.toUpperCase() === row.sizeName.toUpperCase())?.id ?? "" });
            }

            return acc;
        }, [[], []] as [ProductInventoryRequest[], ProductInventoryRequestWithId[]]);

        const deleteProductInventoryIds = productInventory.filter((inventory) => !rows.some((row) => row.sizeName.toLocaleUpperCase() === inventory.size.name.toUpperCase())).map((inventory) => inventory.id);

        if (newProductInventoryRow.length > 0) {
            await Promise.all(newProductInventoryRow.map((inventory) => createProductInventory(inventory).unwrap()));
        }

        if (updateProductInventoryRow.length > 0) {
            await Promise.all(updateProductInventoryRow.map((inventory) => updateProductInventory({
                id: inventory.id,
                productInventory: {
                    idProduct: inventory.idProduct,
                    quantity: inventory.quantity,
                    idSize: inventory.idSize,
                }
            }).unwrap()));
        }

        if (deleteProductInventoryIds.length > 0) {
            await Promise.all(deleteProductInventoryIds.map((id) => deleteProductInventory(id).unwrap()));
        }
        setAlert({
            show: true,
            message: "Product inventory has been updated",
            variant: "success",
        });
        refetch();
    };

    return (
        <div>
            {isGettingSizes ? (
                <Spinner size="large" />
            ) : (
                <div>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Size</TableHead>
                                <TableHead>Quantity</TableHead>
                                <TableHead>Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {rows.map((row, index) => (
                                <TableRow key={index}>
                                    <TableCell>
                                        <Input
                                            type="text"
                                            value={row.sizeName.toUpperCase()}
                                            onChange={(e) => setRows((prev) => prev.map((r, i) => i === index ? { ...r, sizeName: e.target.value } : r))}
                                            className="w-full"
                                        />
                                    </TableCell>

                                    <TableCell>
                                        <Input
                                            type="number"
                                            value={row.quantity === 0 ? "" : row.quantity}
                                            onChange={(e) => {
                                                const value = !!e.target.value ? parseInt(e.target.value) : 0;
                                                setRows((prev) => prev.map((r, i) => i === index ? { ...r, quantity: value } : r));
                                            }}
                                            className="w-full"
                                        />
                                    </TableCell>

                                    <TableCell>
                                        <Button
                                            variant="link"
                                            onClick={() =>
                                                setRows((prev) => prev.filter((_, i) => i !== index))
                                            }
                                        >
                                            <TrashIcon size={18} />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                    <div className="flex justify-between">
                        <Button variant="link" onClick={() => setRows((prev) => [...prev, { sizeName: "", quantity: 0 }])}>
                            Add Row
                        </Button>

                        <Button variant="link" onClick={() => setRows(productInventory.map((inventory) => ({
                            id: inventory.id,
                            sizeName: inventory.size.name,
                            quantity: inventory.quantity,
                        })))}>
                            Reset Row
                        </Button>
                    </div>
                </div>
            )}
            <DialogFooter>
                <Button variant="outline" onClick={onClose}>
                    Close
                </Button>
                <Button type="submit" disabled={isCreatingProductInventory || isUpdatingProductInventory || isDeletingProductInventory || isCreatingSize} onClick={handleSave}>
                    {(isCreatingProductInventory || isUpdatingProductInventory || isDeletingProductInventory || isCreatingSize) ? "Saving..." : "Save"}
                </Button>
            </DialogFooter>
        </div>
    );
};

export default CreateUpdateProductInventoryTab;
