import axiosClient from "./axiosClient";

/**
 * يسجّل مستخدماً جديداً بالنظام.
 * @param {{fullName:string,email:string,password:string,phoneNumber?:string}} payload - بيانات التسجيل
 * @returns Promise يحتوي على access token وبيانات المستخدم
 */
export function register(payload) {
  return axiosClient.post("/auth/register", payload).then((res) => res.data);
}

/**
 * يسجّل دخول مستخدم موجود.
 * @param {{email:string,password:string}} payload - بيانات الدخول
 * @returns Promise يحتوي على access token وبيانات المستخدم
 */
export function login(payload) {
  return axiosClient.post("/auth/login", payload).then((res) => res.data);
}

/**
 * يسجّل خروج المستخدم الحالي ويُبطل refresh token.
 * @returns Promise فارغ عند النجاح
 */
export function logoutRequest() {
  return axiosClient.post("/auth/logout");
}

/**
 * يفعّل البريد الإلكتروني عبر رمز التفعيل المرسل بالإيميل.
 * @param {string} token - رمز التفعيل
 * @returns Promise فارغ عند النجاح
 */
export function verifyEmail(token) {
  return axiosClient.post("/auth/verify-email", { token });
}
