import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { ProductListItem, CreateProductPayload, UpdateProductPayload } from "@/types/product";
import { PagedModel } from "@/types/user";

export interface UseAdminProductsParams {
  page?: number;
  size?: number;
}

export function useAdminProducts({ page = 0, size = 15 }: UseAdminProductsParams = {}) {
  return useQuery({
    queryKey: ["admin-products", { page, size }],
    queryFn: async () => {
      const params: Record<string, string> = {
        page: String(page),
        size: String(size),
      };

      return api.get<PagedModel<ProductListItem>>("/admin/products", {
        params,
      });
    },
    placeholderData: keepPreviousData,
  });
}

export function useCreateProduct() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (payload: CreateProductPayload) =>
      api.post("/admin/products", payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["admin-products"] });
    },
  });
}

export function useUpdateProduct() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateProductPayload }) =>
      api.put(`/admin/products/${id}`, payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["admin-products"] });
    },
  });
}

export function useDeleteProduct() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.delete(`/admin/products/${id}`),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["admin-products"] });
    },
  });
}
