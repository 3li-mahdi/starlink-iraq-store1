import axiosClient from "./axiosClient";

/**
 * تجلب سلة التسوق الحالية (للمستخدم المسجَّل أو الزائر).
 * @returns Promise يحتوي على عناصر السلة والسعر الإجمالي
 */
export function fetchCart() {
  return axiosClient.get("/cart").then((res) => res.data);
}

/**
 * تضيف منتجاً للسلة بكمية معيّنة.
 * @param {number|string} productId - معرّف المنتج
 * @param {number} quantity - الكمية المطلوبة
 * @returns Promise يحتوي على بيانات السلة بعد الإضافة
 */
export function addCartItem(productId, quantity) {
  return axiosClient.post("/cart/items", { productId, quantity }).then((res) => res.data);
}

/**
 * تحدّث كمية عنصر موجود بالسلة.
 * @param {number|string} itemId - معرّف عنصر السلة
 * @param {number} quantity - الكمية الجديدة
 * @returns Promise يحتوي على بيانات السلة بعد التحديث
 */
export function updateCartItem(itemId, quantity) {
  return axiosClient.put(`/cart/items/${itemId}`, { quantity }).then((res) => res.data);
}

/**
 * تحذف عنصراً من السلة.
 * @param {number|string} itemId - معرّف عنصر السلة
 * @returns Promise يحتوي على بيانات السلة بعد الحذف
 */
export function removeCartItem(itemId) {
  return axiosClient.delete(`/cart/items/${itemId}`).then((res) => res.data);
}
