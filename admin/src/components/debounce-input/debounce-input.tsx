import { Input } from '@/components/ui/input';
import React, { useState, useEffect } from 'react';

interface DebouncedInputProps {
    placeholder?: string;
    value: string;
    onChange: (value: string) => void;
    delay?: number;
    className?: string;
}

const DebouncedInput: React.FC<DebouncedInputProps> = ({
    placeholder,
    value: initialValue,
    onChange,
    delay = 300,
    className,
}) => {
    const [value, setValue] = useState(initialValue);

    useEffect(() => {
        const handler = setTimeout(() => {
            onChange(value);
        }, delay);

        return () => {
            clearTimeout(handler);
        };
    }, [value, delay, onChange]);

    return (
        <Input
            placeholder={placeholder}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            className={className}
        />
    );
};

export default DebouncedInput;