import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { AdminUserListItem, PagedModel } from "@/types/user";

export interface UseAdminUsersParams {
    page?: number;
    size?: number;
    search?: string;
}

export function useAdminUsers({ page = 0, size = 15, search = "" }: UseAdminUsersParams = {}) {
    return useQuery({
        queryKey: ["admin-users", { page, size, search }],
        queryFn: async () => {
            const params: Record<string, string> = {
                page: String(page),
                size: String(size),
            };

            const trimmedSearch = search.trim();
            if (trimmedSearch) {
                params.search = trimmedSearch;
            }

            return api.get<PagedModel<AdminUserListItem>>("/admin/users", {
                params,
            });
        },
        placeholderData: keepPreviousData,
    });
}
