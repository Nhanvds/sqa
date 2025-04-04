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

const CustomSelect = ({ value, onChange, options, placeholder }: {
    value: string;
    onChange: (value: string) => void;
    options: {
        label: string;
        value: string;
    }[];
    placeholder?: string;
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
                    className="col-span-3 justify-between min-w-20"
                >
                    {value
                        ? options.find((o) => o.value === value)?.label
                        : `${placeholder || "Select"}`}
                    <ChevronsUpDown className="opacity-50" />
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-full p-0">
                <Command className="w-[300px]">
                    <CommandInput placeholder="Search category..." className="h-9" />
                    <CommandList>
                        <CommandEmpty>No category found.</CommandEmpty>
                        <CommandGroup>
                            {options.map((o) => (
                                <CommandItem
                                    key={o.value}
                                    value={o.label}
                                    onSelect={() => {
                                        onChange(o.value);
                                    }}
                                >
                                    {o.label}
                                    <Check
                                        className={cn(
                                            "ml-auto",
                                            value === o.value ? "opacity-100 text-primary" : "opacity-0"
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

export default CustomSelect;