import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import { OrderListItem, PaginatedOrders, OrderStatus } from "@/types/order";

interface UseAdminOrdersParams {
  page?: number;
  size?: number;
  status?: OrderStatus | "";
}

export function useAdminOrders({ page = 0, size = 10, status = "" }: UseAdminOrdersParams = {}) {
  const params: Record<string, string> = {
    page: page.toString(),
    size: size.toString(),
  };

  if (status) {
    params.status = status;
  }

  return useQuery({
    queryKey: ["admin-orders", page, size, status],
    queryFn: async () => {
      const response = await api.get<PaginatedOrders>("/admin/orders", { params });
      return response;
    },
  });
}

export function useUpdateOrderStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: OrderStatus }) =>
      api.put<OrderListItem>(`/admin/orders/${id}/status`, { status }),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["admin-orders"] });
    },
  });
}
