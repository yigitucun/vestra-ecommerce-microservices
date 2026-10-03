import type { Metadata } from 'next';
import {ResetPasswordForm} from "@/app/auth/reset-password/_component/reset-password-form";
import {redirect} from "next/navigation";

export const metadata:Metadata = {
    title: "Şifremi yenile",
}
interface PageProps {
    searchParams: Promise<{
        token?: string;
    }>
}
export default async function Page({searchParams}: PageProps) {
    const {token} = await searchParams
    if (!token) redirect("/")
    return (
        <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-background p-6 md:p-10">
            <div className="w-full max-w-xs">
                <ResetPasswordForm token={token} />
            </div>
        </div>
    )
}