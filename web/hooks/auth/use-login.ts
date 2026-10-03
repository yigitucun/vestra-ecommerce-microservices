import {useMutation} from "@tanstack/react-query";
import {api} from "@/lib/api";
import {LoginSchema} from "@/types/user";
import {useRouter} from "next/navigation";


export function useLogin(){
    const router = useRouter();
    return useMutation({
        mutationFn: (values: LoginSchema) => api.post("/auth/login",values),
        onSuccess: () => router.replace("/")
    })
}