import type { Metadata } from "next";
import { ProductsDataTable } from "@/app/dashboard/products/_components/products-data-table";

export const metadata: Metadata = {
  title: "Ürünler",
  description: "Ürün kataloğu ve yönetimi",
};

export default function Page() {
  return (
    <div className="flex flex-col gap-6">
      <ProductsDataTable />
    </div>
  );
}