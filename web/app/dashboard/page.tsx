import type { Metadata } from "next";
import { DashboardOverview } from "./_components/dashboard-overview";

export const metadata: Metadata = {
  title: "Genel Bakış | Vestra Yönetim",
  description: "Vestra e-ticaret yönetim paneli performans, ciro ve mikroservis genel bakışı",
};

export default function Page() {
  return <DashboardOverview />;
}
