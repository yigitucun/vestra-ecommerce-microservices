import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { AdminUserListItem, PagedModel, UpdateUserPayload } from "@/types/user";

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

export function useUpdateUser() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, payload }: { id: string; payload: UpdateUserPayload }) =>
            api.put(`/admin/users/${id}`, payload),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: ["admin-users"] });
        },
    });
}

export function useDeleteUser() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => api.delete(`/admin/users/${id}`),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: ["admin-users"] });
        },
    });
}
