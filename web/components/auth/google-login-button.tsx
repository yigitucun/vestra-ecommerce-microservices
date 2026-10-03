"use client"

import {Button} from "@/components/ui/button";
const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL


export function GoogleLoginButton(){

    const handleGoogleLogin = () => {
        window.location.href = `${BASE_URL}/auth/oauth2/authorization/google`
    }

    return (
        <Button onClick={handleGoogleLogin} variant="outline" type="button">
            <img width={16} src="/icons/google-icon.png" alt="Google Login"/>
            Google ile devam et
        </Button>
    )
}