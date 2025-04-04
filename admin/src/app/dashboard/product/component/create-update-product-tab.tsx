"use client";

import { Controller, useForm } from "react-hook-form";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import CategorySelect from "@/app/dashboard/product/component/category-select";
import PromotionsSelect from "@/app/dashboard/product/component/promotion-select";
import { StatusEnum } from "@/types/enums";
import { ProductResponse } from "@/types/product";
import { CategoryResponse } from "@/types/category";
import { PromotionResponse } from "@/types/promotion";
import { useCreateProductMutation, useUpdateProductMutation } from "@/redux/api/product-api";
import { useUploadImageMutation } from "@/redux/api/upload-image";
import { useEffect, useState } from "react";
import Image from "next/image";
import { DialogFooter } from "@/components/ui/dialog";
import { CustomAlertProps } from "@/components/ui/CustomAlert";

interface ProductTabProps {
    product?: ProductResponse;
    categories: CategoryResponse[];
    promotions: PromotionResponse[];
    refetch: () => void;
    onClose: () => void;
    setAlert: (alert: CustomAlertProps) => void;
    setProductId: (id: string) => void;
}

const CreateUpdateProductTab = ({ product, categories, promotions, refetch, onClose, setAlert, setProductId }: ProductTabProps) => {
    const { register, handleSubmit, control, formState: { errors }, reset, setValue } = useForm({
        defaultValues: {
            name: product?.name ?? "",
            description: product?.description ?? "",
            price: product?.price ?? 0,
            categoryId: product?.categoryResponse?.id ?? "",
            status: product?.status ? product.status === StatusEnum.ACTIVE : true,
            promotionIds: product?.promotions.map((promotion) => promotion.id) ?? [],
            mainImage: product?.mainImage ?? "",
        },
    });
    const [createProduct, { isLoading: isCreating }] = useCreateProductMutation();
    const [updateProduct, { isLoading: isUpdating }] = useUpdateProductMutation();
    const [uploadImage, { isLoading: isUploading }] = useUploadImageMutation();

    useEffect(() => {
        if(!product) {
            reset();
        }
    }, [product]);

    const [imagePreview, setImagePreview] = useState<string | null>(
            product?.mainImage ? product.mainImage : null
        );

    const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file) {
            const formData = new FormData();
            formData.append("file", file);
            uploadImage(formData)
                .unwrap()
                .then((response) => {
                    if (response.result) {
                        setValue("mainImage", response.result);
                        setImagePreview(response.result);
                    }
                })
                .catch((error) => {
                    setAlert({
                        show: true,
                        message: error.data?.message ?? "An error occurred",
                        variant: "destructive",
                    });
                });
        }
    };

    const handleRemoveImage = () => {
        setValue("mainImage", "");
        setImagePreview(null);
    };

    const onSubmit = async (data: any) => {
        try {
            if (product) {
                await updateProduct({
                    id: product.id,
                    product: {
                        ...data,
                        status: data.status ? StatusEnum.ACTIVE : StatusEnum.INACTIVE,
                    },
                }).unwrap();
            } else {
                await createProduct({
                    ...data,
                    status: data.status ? StatusEnum.ACTIVE : StatusEnum.INACTIVE,
                }).unwrap().then((response) => {
                 setProductId(response.result ?? "");
                });
            }
            setAlert({
                show: true,
                message: `Product ${product ? "updated" : "created"} successfully`,
                variant: "success",
            });
            refetch();
        } catch (error: any) {
            setAlert({
                show: true,
                message: error.data?.message ?? "An error occurred",
                variant: "destructive",
            });
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4 py-5">
            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="name" className="text-right">
                    Name <span className="text-red-500">*</span>
                </Label>
                <Input
                    id="name"
                    {...register("name", { required: "Name is required" })}
                    className={`col-span-3 ${errors.name ? "border-red-500" : ""}`}
                />
                {errors.name && <p className="col-span-4 text-red-500 text-sm">{errors.name.message}</p>}
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="description" className="text-right">
                    Description
                </Label>
                <Input id="description" {...register("description")} className="col-span-3" />
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="price" className="text-right">
                    Price <span className="text-red-500">*</span>
                </Label>
                <Input
                    type="number"
                    min={0}
                    id="price"
                    {...register("price", { required: "Price is required" })}
                    className={`col-span-3 ${errors.price ? "border-red-500" : ""}`}
                />
                {errors.price && <p className="col-span-4 text-red-500 text-sm">{errors.price.message}</p>}
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="categoryId" className="text-right">
                    Category <span className="text-red-500">*</span>
                </Label>
                <Controller
                    name="categoryId"
                    control={control}
                    rules={{ required: "Category is required" }}
                    render={({ field: { value, onChange } }) => (
                        <CategorySelect value={value} onChange={onChange} categories={categories} />
                    )}
                />
                {errors.categoryId && (
                    <p className="col-span-4 text-red-500 text-sm">{errors.categoryId.message}</p>
                )}
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="promotionIds" className="text-right">
                    Promotions
                </Label>
                <Controller
                    name="promotionIds"
                    control={control}
                    render={({ field: { value, onChange } }) => (
                        <PromotionsSelect value={value} onChange={onChange} promotions={promotions} />
                    )}
                />
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="mainImage" className="text-right">
                    Image <span className="text-red-500">*</span>
                </Label>
                <div className="col-span-3">
                    {imagePreview ? (
                        <div className="relative mb-2">
                            <Image
                                src={imagePreview}
                                alt="Preview"
                                width={80}
                                height={80}
                                className="object-cover"
                            />
                            <Button
                                variant="outline"
                                size="icon"
                                className="absolute top-0 right-0"
                                onClick={handleRemoveImage}
                            >
                                X
                            </Button>
                        </div>
                    ) : (
                        <Input
                            id="mainImage"
                            type="file"
                            accept="image/*"
                            onChange={handleImageChange}
                            disabled={isUploading}
                        />
                    )}
                </div>
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="status" className="text-right">
                    Status <span className="text-red-500">*</span>
                </Label>
                <Controller
                    name="status"
                    control={control}
                    render={({ field }) => (
                        <Checkbox id="status" checked={field.value} onCheckedChange={field.onChange} />
                    )}
                />
            </div>

            <DialogFooter>
                <Button variant="outline" onClick={onClose}>
                    Close
                </Button>
                <Button type="submit" disabled={isCreating || isUpdating}>
                    {(isCreating || isUpdating) ? "Saving..." : "Save"}
                </Button>
            </DialogFooter>
        </form>
    );
};

export default CreateUpdateProductTab;
