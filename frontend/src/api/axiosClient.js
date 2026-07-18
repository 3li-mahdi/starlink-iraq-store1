import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const axiosClient = axios.create({
  baseURL: BASE_URL,
  withCredentials: true, // يسمح بإرسال كوكي httpOnly الخاص بـ refresh token
});

let accessToken = null;
let storeRef = null;
let refreshPromise = null;

/**
 * يخزّن access token الحالي بالذاكرة ليُستخدم بكل طلب لاحق.
 * @param {string|null} token - قيمة access token الجديدة، أو null لمسحها
 */
export function setAccessToken(token) {
  accessToken = token;
}

/**
 * يربط axiosClient بمتجر Redux بعد إنشائه، لتفادي الاستيراد الدائري بين الملفين.
 * @param {object} store - متجر Redux الرئيسي للتطبيق
 */
export function injectStore(store) {
  storeRef = store;
}

axiosClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  const guestSessionId = localStorage.getItem("guestSessionId");
  if (guestSessionId) {
    config.headers["X-Guest-Session-Id"] = guestSessionId;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const isAuthEndpoint = originalRequest?.url?.includes("/auth/");

    if (status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      originalRequest._retry = true;
      try {
        const newToken = await refreshAccessToken();
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return axiosClient(originalRequest);
      } catch (refreshError) {
        if (storeRef) {
          const { logout } = await import("../features/auth/authSlice");
          storeRef.dispatch(logout());
        }
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

/**
 * يجدّد access token عبر refresh token المخزَّن بكوكي httpOnly، مع دمج الطلبات المتزامنة
 * بحيث لا يُرسَل أكثر من طلب تجديد واحد بنفس اللحظة.
 * @returns {Promise<string>} access token الجديد
 */
function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${BASE_URL}/auth/refresh`, {}, { withCredentials: true })
      .then((response) => {
        const token = response.data.accessToken;
        setAccessToken(token);
        return token;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

export default axiosClient;
