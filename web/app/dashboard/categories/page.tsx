import type { Metadata } from "next";
import { CategoriesDataTable } from "@/app/dashboard/categories/_components/categories-data-table";

export const metadata: Metadata = {
  title: "Kategoriler",
  description: "Kategori listesi ve yönetimi",
};

export default function Page() {
  return (
    <div className="flex flex-col gap-6">
      <CategoriesDataTable />
    </div>
  );
}