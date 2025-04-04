import React, { useState, useEffect } from 'react';
import { Input } from 'antd';
import { CloseCircleFilled } from '@ant-design/icons';

interface DebouncedInputProps {
    placeholder?: string;
    value?: string;
    onDebouncedChange: (value: string) => void;
    delay?: number;
    className?: string;
}

const DebouncedInput: React.FC<DebouncedInputProps> = ({
    placeholder,
    value: initialValue,
    onDebouncedChange,
    delay = 500,
    className,
}) => {
    const [value, setValue] = useState(initialValue || '');

    useEffect(() => {
        const handler = setTimeout(() => {
            onDebouncedChange(value);
        }, delay);

        return () => {
            clearTimeout(handler);
        };
    }, [value, delay]);

    const handleClear = () => {
        setValue('');
        onDebouncedChange('');
    };

    return (
        <Input
            placeholder={placeholder}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            onPressEnter={() => onDebouncedChange(value)}
            className={className}
            suffix={
                value ? (
                    <CloseCircleFilled
                        onClick={handleClear}
                        className='cursor-pointer text-color-secondary'
                    />
                ) : null
            }
        />
    );
};

export default DebouncedInput;