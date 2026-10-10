import type { Metadata } from 'next';
import { OrdersDataTable } from '@/app/dashboard/orders/_components/orders-data-table';

export const metadata: Metadata = {
    title: "Siparişler | Vestra Yönetim",
    description: "Müşteri sipariş listesi ve sipariş yönetimi",
};

export default function Page() {
    return (
        <div className="flex flex-col gap-6">
            <OrdersDataTable />
        </div>
    );
}