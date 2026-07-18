import axiosClient from "./axiosClient";

/**
 * تجلب قائمة رغبات المستخدم الحالي.
 * @returns Promise يحتوي على قائمة المنتجات المحفوظة
 */
export function fetchWishlist() {
  return axiosClient.get("/wishlist").then((res) => res.data);
}

/**
 * تضيف منتجاً لقائمة رغبات المستخدم.
 * @param {number|string} productId - معرّف المنتج
 * @returns Promise فارغ عند النجاح
 */
export function addToWishlist(productId) {
  return axiosClient.post(`/wishlist/${productId}`);
}

/**
 * تحذف منتجاً من قائمة رغبات المستخدم.
 * @param {number|string} productId - معرّف المنتج
 * @returns Promise فارغ عند النجاح
 */
export function removeFromWishlist(productId) {
  return axiosClient.delete(`/wishlist/${productId}`);
}
