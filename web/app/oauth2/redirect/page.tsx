"use client"

import { Suspense, useEffect, useRef } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { api } from "@/lib/api"

function OAuth2RedirectContent() {
    const searchParams = useSearchParams()
    const router = useRouter()
    const exchanged = useRef(false)

    useEffect(() => {
        const code = searchParams.get("code")

        if (!code) {
            router.replace("/auth/login?error=oauth")
            return
        }

        if (exchanged.current) return
        exchanged.current = true

        const exchange = async () => {
            try {
                await api.post(`/auth/oauth2/exchange?code=${encodeURIComponent(code)}`, {})
                router.replace("/")
            } catch {
                router.replace("/auth/login?error=oauth")
            }
        }
        void exchange()
    }, [searchParams, router])

    return (
        <div className="flex min-h-svh items-center justify-center">
            Google ile giriş yapılıyor...
        </div>
    )
}

export default function OAuth2RedirectPage() {
    return (
        <Suspense
            fallback={
                <div className="flex min-h-svh items-center justify-center">
                    Yükleniyor...
                </div>
            }
        >
            <OAuth2RedirectContent />
        </Suspense>
    )
}
