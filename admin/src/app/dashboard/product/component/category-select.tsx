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
import { CategoryResponse } from "@/types/category";
import { cn } from "@/lib/utils";

const CategorySelect = ({value, onChange, categories}: {
    value: string;
    onChange: (value: string) => void;
    categories: CategoryResponse[];
}) => {
    const [isOpenSelectCategory, setIsOpenSelectCategory] = useState(false);
    return (
        <Popover
            open={isOpenSelectCategory}
            onOpenChange={setIsOpenSelectCategory}
        >
            <PopoverTrigger asChild>
                <Button
                    variant="outline"
                    role="combobox"
                    aria-expanded={Boolean(value)}
                    className="col-span-3 justify-between"
                >
                    {value
                        ? categories.find((cat) => cat.id === value)?.name
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
                            {categories.map((category) => (
                                <CommandItem
                                    key={category.id}
                                    value={category.name}
                                    onSelect={() => {
                                        onChange(category.id);
                                    }}
                                >
                                    {category.name}
                                    <Check
                                        className={cn(
                                            "ml-auto",
                                            value === category.id ? "opacity-100 text-primary" : "opacity-0"
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

export default CategorySelect;