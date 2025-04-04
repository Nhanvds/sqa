type DebounceFunction<T extends (...args: any[]) => void> = (
    ...args: Parameters<T>
) => void;

export function debounce<T extends (...args: any[]) => void>(
    callback: T,
    delay: number
): DebounceFunction<T> {
    let timeoutId: NodeJS.Timeout | null = null;

    return (...args: Parameters<T>) => {
        if (timeoutId) {
            clearTimeout(timeoutId);
        }
        timeoutId = setTimeout(() => {
            callback(...args);
        }, delay);
    };
}