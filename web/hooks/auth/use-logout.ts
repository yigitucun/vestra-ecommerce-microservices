import {useMutation, useQueryClient} from "@tanstack/react-query";
import {api} from "@/lib/api";
import {useRouter} from "next/navigation";


export const  useLogout = () => {
    const router = useRouter();
    const client = useQueryClient()
    return useMutation({
        mutationFn: () => api.post("/auth/logout",{}),
        onSuccess: () => {
            void client.removeQueries({queryKey:["me"]})
            router.replace("/auth/login")
        }
    })
}