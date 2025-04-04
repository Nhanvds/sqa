import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from "@/components/ui/command"
import { useState } from "react";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { ChevronsUpDown, Check } from "lucide-react";
import { cn } from "@/lib/utils";
import { ShippingAddressResponse } from "@/types/shipping-address";

const ShippingAddressSelect = ({ value, onChange, shippingAddresses }: {
    value: string;
    onChange: (value: string) => void;
    shippingAddresses: ShippingAddressResponse[];
}) => {
    const [isOpenSelect, setIsOpenSelect] = useState(false);
    return (
        <Popover
            open={isOpenSelect}
            onOpenChange={setIsOpenSelect}
        >
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={Boolean(value)}
                    className="col-span-3 justify-between"
                >
                    {value
                        ? shippingAddresses.find((sa) => sa.id === value)?.addressDetail
                        : "Select Category"}
                    <ChevronsUpDown className="opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-full p-0">
                <Command className="w-[300px]">
                    <CommandInput placeholder="Search category..." className="h-9" />
                    <CommandList>
                        <CommandEmpty>No category found.</CommandEmpty>
                        <CommandGroup>
                            {shippingAddresses.map((sa) => (
                                <CommandItem
                                    key={sa.id}
                                    value={sa.addressDetail}
                                    onSelect={() => {
                                        onChange(sa.id);
                                    }}
                                >
                                    {sa.addressDetail}
                                    <Check
                                        className={cn(
                                            "ml-auto",
                                            value === sa.id ? "opacity-100 text-primary" : "opacity-0"
                                        )}
                                    />
                                </CommandItem>
                            ))}
                        </CommandGroup>
                    </CommandList>
                </Command>
            </PopoverContent>
        </Popover>
    )
}

export default ShippingAddressSelect;