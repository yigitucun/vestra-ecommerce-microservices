
import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import {User} from "@/types/user";

export const useMe = () => {
    const isAuthenticated = typeof document !== 'undefined'
        ? document.cookie.includes('is_authenticated=true')
        : false;

    return useQuery({
        queryKey: ["me"],
        queryFn: () => api.get<User>("/users/me"),
        enabled: isAuthenticated,
        retry: false,
    });
}