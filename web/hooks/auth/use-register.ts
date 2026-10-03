import {useMutation} from "@tanstack/react-query";
import {api} from "@/lib/api";
import {RegisterSchema} from "@/types/user";
import {useRouter} from "next/navigation";


export function useRegister() {
    const router = useRouter()
    return useMutation({
        mutationFn: (values: RegisterSchema) => api.post("/auth/register",values),
        onSuccess: () => router.replace("/")
    })
}