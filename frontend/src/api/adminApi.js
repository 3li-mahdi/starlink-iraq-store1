import axiosClient from "./axiosClient";

/**
 * تُنشئ منتجاً جديداً (للأدمن فقط).
 * @param {object} payload - بيانات المنتج
 * @returns Promise يحتوي على بيانات المنتج بعد الإنشاء
 */
export function createProduct(payload) {
  return axiosClient.post("/admin/products", payload).then((res) => res.data);
}

/**
 * تعدّل منتجاً موجوداً (للأدمن فقط).
 * @param {number|string} id - معرّف المنتج
 * @param {object} payload - البيانات الجديدة
 * @returns Promise يحتوي على بيانات المنتج بعد التعديل
 */
export function updateProduct(id, payload) {
  return axiosClient.put(`/admin/products/${id}`, payload).then((res) => res.data);
}

/**
 * تحذف (تعطّل) منتجاً (للأدمن فقط).
 * @param {number|string} id - معرّف المنتج
 * @returns Promise فارغ عند النجاح
 */
export function deleteProduct(id) {
  return axiosClient.delete(`/admin/products/${id}`);
}

/**
 * تجلب كل الطلبات بالنظام (للأدمن فقط).
 * @param {number} page - رقم الصفحة
 * @returns Promise يحتوي على قائمة الطلبات
 */
export function fetchAllOrders(page = 0) {
  return axiosClient.get("/admin/orders", { params: { page, size: 20 } }).then((res) => res.data);
}

/**
 * تحدّث حالة طلب معيّن (للأدمن فقط).
 * @param {number|string} orderId - معرّف الطلب
 * @param {string} status - الحالة الجديدة
 * @returns Promise يحتوي على بيانات الطلب بعد التحديث
 */
export function updateOrderStatus(orderId, status) {
  return axiosClient.put(`/admin/orders/${orderId}/status`, { status }).then((res) => res.data);
}

/**
 * تُنشئ كوبون خصم جديداً (للأدمن فقط).
 * @param {object} payload - بيانات الكوبون
 * @returns Promise يحتوي على بيانات الكوبون بعد الإنشاء
 */
export function createCoupon(payload) {
  return axiosClient.post("/admin/coupons", payload).then((res) => res.data);
}

/**
 * تجلب كل الكوبونات (للأدمن فقط).
 * @returns Promise يحتوي على قائمة الكوبونات
 */
export function fetchAllCoupons() {
  return axiosClient.get("/admin/coupons").then((res) => res.data);
}

/**
 * تفعّل أو تعطّل كوبوناً (للأدمن فقط).
 * @param {number|string} id - معرّف الكوبون
 * @param {boolean} isActive - الحالة الجديدة
 * @returns Promise يحتوي على بيانات الكوبون بعد التعديل
 */
export function setCouponActive(id, isActive) {
  return axiosClient.put(`/admin/coupons/${id}/active`, null, { params: { isActive } }).then((res) => res.data);
}

/**
 * تجلب سجلات التدقيق الأمني (للأدمن فقط).
 * @param {number} page - رقم الصفحة
 * @returns Promise يحتوي على قائمة سجلات التدقيق
 */
export function fetchAuditLogs(page = 0) {
  return axiosClient.get("/admin/audit-logs", { params: { page, size: 30 } }).then((res) => res.data);
}
