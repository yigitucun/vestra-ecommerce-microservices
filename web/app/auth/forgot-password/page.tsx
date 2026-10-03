import type { Metadata } from 'next';
import {ForgotPasswordForm} from "@/app/auth/forgot-password/_component/forgot-password-form";

export const metadata:Metadata = {
    title: "Şifremi unuttum"
}


export default function Page(){
    return (
        <div className="flex min-h-svh flex-col items-center justify-center gap-6 bg-background p-6 md:p-10">
            <div className="w-full max-w-xs">
                <ForgotPasswordForm />
            </div>
        </div>
    )
}