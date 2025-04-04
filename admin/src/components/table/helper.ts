"use client";

import { z } from "zod";

export const localStorageKeyPrefixes = {
    searchValuePrefix: "search-value-",
    tableSortByPrefix: "table-sort-by-",
    tablePageSizePrefix: "table-page-size-",
};

export const tableLocalStorageKeys = {
    sortBy: (pathname: string) =>
        `${localStorageKeyPrefixes.tableSortByPrefix}${pathname}`,
    pageSize: (pathname: string) =>
        `${localStorageKeyPrefixes.tablePageSizePrefix}${pathname}`,
};

export const getTableSavedSortingState = (pathKey: string) => {
    const savedSorting = localStorage.getItem(
        tableLocalStorageKeys.sortBy(pathKey)
    );
    if (savedSorting && savedSorting !== "") {
        try {
            const parsedResult = z
                .array(
                    z.object({
                        id: z.string(),
                        desc: z.boolean(),
                    })
                )
                .safeParse(JSON.parse(savedSorting));

            if (parsedResult.success) {
                return parsedResult.data;
            } else {
                console.error(
                    `Parsing data ${savedSorting} to JSON error: ${parsedResult.error.message}`
                );
                return [];
            }
        } catch (error: unknown) {
            console.error(error);
            return [];
        }
    }
    return [];
};

export const getTableSavedPageSize = (
    pathKey: string,
    initialPageIndex: number,
    initialPageSize: number
) => {
    const savedPageSize = localStorage.getItem(
        tableLocalStorageKeys.pageSize(pathKey)
    );
    if (savedPageSize && savedPageSize !== "") {
        try {
            const parsedResult = z.number().safeParse(JSON.parse(savedPageSize));

            if (parsedResult.success) {
                return {
                    pageIndex: initialPageIndex,
                    pageSize: parsedResult.data,
                };
            } else {
                console.error(
                    `Parsing data ${savedPageSize} to JSON error: ${parsedResult.error.message}`
                );
                return {
                    pageIndex: initialPageIndex,
                    pageSize: initialPageSize,
                };
            }
        } catch (error: unknown) {
            console.error(error);
            return {
                pageIndex: initialPageIndex,
                pageSize: initialPageSize,
            };
        }
    }
    return {
        pageIndex: initialPageIndex,
        pageSize: initialPageSize,
    };
};

export const flattenSortBy = (
    sortBy: Record<string, unknown>,
    prefix: string,
    accKey: string
): Record<string, unknown> => {
    const key = Object.keys(sortBy)[0];
    if (!key) return sortBy;
    if (typeof sortBy[key] === "object") {
        return flattenSortBy(
            // type-coverage:ignore-next-line
            sortBy[key] as Record<string, unknown>,
            prefix,
            `${accKey}${key}${prefix}`
        );
    }
    return { [`${accKey}${key}`]: sortBy[key] };
};