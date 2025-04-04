import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Check, ChevronsUpDown } from "lucide-react";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { PromotionResponse } from "@/types/promotion";
import { cn } from "@/lib/utils";

const PromotionsSelect = ({ value, onChange, promotions }: {
    value: string[];
    onChange: (value: string[]) => void;
    promotions: PromotionResponse[];
}) => {
    const [isOpenSelectPromotion, setIsOpenSelectPromotion] = useState(false);

    const handleSelectPromotion = (promotionId: string) => {
        const updatedValue = value.includes(promotionId)
            ? value.filter((id) => id !== promotionId)
            : [...value, promotionId];
        onChange(updatedValue);
    };

    return (
        <Popover open={isOpenSelectPromotion} onOpenChange={setIsOpenSelectPromotion}>
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={Boolean(value.length)}
                    className="col-span-3 justify-between"
                >
                    {value.length ? `${value.length} promotions selected` : "Select Promotions"}
                    <ChevronsUpDown className="opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-full p-0">
                <Command className="w-[300px]">
                    <CommandInput placeholder="Search promotions..." className="h-9" />
                    <CommandList>
                        <CommandEmpty>No promotions found.</CommandEmpty>
                        <CommandGroup>
                            {promotions.map((promotion) => (
                                <CommandItem key={promotion.id} onSelect={() => handleSelectPromotion(promotion.id)}>
                                    <div className="flex justify-between w-full">
                                        <span>{promotion.name}</span>
                                        <Check
                                            className={cn(
                                                "ml-auto",
                                                value.includes(promotion.id) ? "opacity-100 text-primary" : "opacity-0"
                                            )}
                                        />
                                    </div>
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command>
            </PopoverContent>
        </Popover>
    );
};

export default PromotionsSelect;
