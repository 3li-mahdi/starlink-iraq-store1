import axiosClient from "./axiosClient";

/**
 * تنفّذ عملية الشراء (checkout) للسلة الحالية.
 * @param {{idempotencyKey:string,shippingAddress?:string,couponCode?:string}} payload - بيانات الشراء
 * @returns Promise يحتوي على بيانات الطلب المكتمل
 */
export function checkout(payload) {
  return axiosClient.post("/checkout", payload).then((res) => res.data);
}

/**
 * تتحقق من صلاحية كوبون خصم لمبلغ طلب معيّن.
 * @param {string} code - كود الكوبون
 * @param {number} orderAmount - المبلغ الإجمالي التقريبي للطلب
 * @returns Promise يحتوي على نتيجة التحقق وقيمة الخصم
 */
export function validateCoupon(code, orderAmount) {
  return axiosClient
    .post("/coupons/validate", { code }, { params: { orderAmount } })
    .then((res) => res.data);
}

/**
 * تجلب تفاصيل طلب واحد عبر معرّفه.
 * @param {number|string} orderId - معرّف الطلب
 * @returns Promise يحتوي على بيانات الطلب
 */
export function fetchOrder(orderId) {
  return axiosClient.get(`/orders/${orderId}`).then((res) => res.data);
}

/**
 * تجلب كل طلبات مستخدم معيّن (لصفحة "طلباتي").
 * @param {number|string} userId - معرّف المستخدم
 * @param {number} page - رقم الصفحة
 * @returns Promise يحتوي على قائمة الطلبات
 */
export function fetchUserOrders(userId, page = 0) {
  return axiosClient.get(`/orders/user/${userId}`, { params: { page, size: 10 } }).then((res) => res.data);
}
