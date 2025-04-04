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
import { User } from "@/types/user-info";

const UserSelect = ({ value, onChange, users }: {
    value: string;
    onChange: (value: string) => void;
    users: User[];
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
                        ? users.find((user) => user.id === value)?.email
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
                            {users.map((user) => (
                                <CommandItem
                                    key={user.id}
                                    value={user.email}
                                    onSelect={() => {
                                        onChange(user.id);
                                    }}
                                >
                                    {user.email}
                                    <Check
                                        className={cn(
                                            "ml-auto",
                                            value === user.id ? "opacity-100 text-primary" : "opacity-0"
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

export default UserSelect;