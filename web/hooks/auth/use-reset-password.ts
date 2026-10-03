import {useMutation} from "@tanstack/react-query";
import {ResetPasswordSchema} from "@/types/user";
import {api} from "@/lib/api";
import {useRouter} from "next/navigation";
import {toast} from "@/components/ui/toast";


export function useResetPassword() {
    const router = useRouter()
    return useMutation({
        mutationFn: (values:ResetPasswordSchema) => api.post("/auth/reset-password", values),
        onSuccess: () => {
            toast.add({title:"Şifreniz yenilendi",description:"Lütfen tekrar giriş yapın",type:"success"});
            router.replace("/auth/login");
        }
    })
}