import type { Metadata } from "next";
import { UsersDataTable } from "@/app/dashboard/users/_components/users-data-table";

export const metadata: Metadata = {
  title: "Kullanıcılar",
  description: "Kullanıcı listesi ve yönetimi",
};

export default function Page() {
  return (
    <div className="flex flex-col gap-6">
      <UsersDataTable />
    </div>
  );
}