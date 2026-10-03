import {useMutation} from "@tanstack/react-query";
import {ForgotPasswordSchema} from "@/types/user";
import {api} from "@/lib/api";
import {toast} from "@/components/ui/toast";


export function useForgotPassword() {
    return useMutation({
        mutationFn: (values: ForgotPasswordSchema) => api.post("/auth/forgot-password", values),
        onSuccess: () => toast.add({type:'success',description:'Bu e-posta kayıtlıysa sana bir bağlantı gönderdik.'}),
    })
}