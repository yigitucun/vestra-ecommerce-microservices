export type OrderStatus =
  | "PENDING"
  | "STOCK_CONFIRMED"
  | "STOCK_FAILED"
  | "PAID"
  | "PAYMENT_FAILED"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED";

export interface OrderItem {
  id: string;
  variantId: string;
  productName: string;
  sku: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderListItem {
  id: string;
  orderNumber: string;
  userId: string;
  customerEmail: string;
  status: OrderStatus;
  totalAmount: number;
  shippingAddress: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface PaginatedOrders {
  content: OrderListItem[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

export interface UpdateOrderStatusPayload {
  status: OrderStatus;
}
