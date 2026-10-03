import {Metadata} from "next";
import {SignupForm} from "@/app/auth/(auth)/signup/_component/signup-form";

export const metadata:Metadata = {
    title: "Kayıt ol",
}

export default async function Page(){
    return <SignupForm/>
}