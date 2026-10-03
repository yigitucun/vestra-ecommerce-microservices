import { ApiError } from "@/lib/api-error";

const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

const AUTH_ENDPOINTS = [
  "/auth/refresh",
  "/auth/login",
  "/auth/register",
  "/auth/forgot-password",
  "/auth/reset-password",
];

interface FetchOptions extends RequestInit {
  params?: Record<string, string>;
  _retryCount?: number;
}

const MAX_RETRY = 1;

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(undefined);
    }
  });
  failedQueue = [];
};

async function parseBody<T>(response: Response): Promise<T> {
  const text = await response.text();
  if (!text) {
    return {} as T;
  }
  return JSON.parse(text) as T;
}

async function fetchWrapper<T>(endpoint: string, options: FetchOptions = {}): Promise<T> {
  const { params, headers, _retryCount = 0, ...customOptions } = options;

  const url = new URL(`${BASE_URL}${endpoint}`);
  if (params) {
    Object.keys(params).forEach((key) => url.searchParams.append(key, params[key]));
  }

  const defaultHeaders: HeadersInit = {
    "Content-Type": "application/json",
  };

  const config: RequestInit = {
    ...customOptions,
    credentials: "include",
    headers: {
      ...defaultHeaders,
      ...headers,
    },
  };

  try {
    const response = await fetch(url.toString(), config);

    const isAuthEndpoint = AUTH_ENDPOINTS.includes(endpoint);

    if (response.status === 401 && !isAuthEndpoint) {
      if (_retryCount >= MAX_RETRY) {
        throw new ApiError("Oturum süresi doldu, lütfen tekrar giriş yapın.", 401, {});
      }

      if (!isRefreshing) {
        isRefreshing = true;

        try {
          const refreshResponse = await fetch(`${BASE_URL}/auth/refresh`, {
            method: "POST",
            credentials: "include",
          });

          if (!refreshResponse.ok) {
            throw new Error("Refresh token patladı");
          }

          isRefreshing = false;
          processQueue(null);

          return await fetchWrapper<T>(endpoint, { ...options, _retryCount: _retryCount + 1 });
        } catch (refreshError) {
          isRefreshing = false;
          processQueue(refreshError);
          window.location.href="/auth/login"
          throw new ApiError("Oturum süresi doldu, lütfen tekrar giriş yapın.", 401, {});
        }
      }

      return new Promise((resolve, reject) => {
        failedQueue.push({
          resolve: () => resolve(fetchWrapper<T>(endpoint, { ...options, _retryCount: _retryCount + 1 })),
          reject: (err) => reject(err),
        });
      });
    }

    if (!response.ok) {
      const errorData = await parseBody<Record<string, string>>(response).catch(
          () => ({}) as Record<string, string>
      );
      throw new ApiError(
          errorData.detail || errorData.message || `API Hatası: ${response.status} ${response.statusText}`,
          response.status,
          { title: errorData.title, instance: errorData.instance }
      );
    }

    if (response.status === 204) {
      return {} as T;
    }

    return await parseBody<T>(response);
  } catch (error) {
    console.error(`[API Error] ${options.method || "GET"} ${endpoint}:`, error);
    throw error;
  }
}

export const api = {
  get: <T>(endpoint: string, options?: Omit<FetchOptions, "method" | "body">) =>
      fetchWrapper<T>(endpoint, { ...options, method: "GET" }),

  post: <T>(endpoint: string, data: unknown, options?: Omit<FetchOptions, "method" | "body">) =>
      fetchWrapper<T>(endpoint, { ...options, method: "POST", body: JSON.stringify(data) }),

  put: <T>(endpoint: string, data: unknown, options?: Omit<FetchOptions, "method" | "body">) =>
      fetchWrapper<T>(endpoint, { ...options, method: "PUT", body: JSON.stringify(data) }),

  delete: <T>(endpoint: string, options?: Omit<FetchOptions, "method" | "body">) =>
      fetchWrapper<T>(endpoint, { ...options, method: "DELETE" }),
};