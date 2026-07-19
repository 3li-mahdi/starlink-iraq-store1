import axiosClient from "./axiosClient";

/**
 * تجلب قائمة المنتجات من الـ API مع دعم الفلترة والصفحات (pagination).
 * تُستدعى عند تحميل صفحة المتجر أو عند تغيير الفلتر من المستخدم.
 *
 * @param {object} filters - يحتوي على: الفئة، نطاق السعر، نوع المنتج، نص البحث
 * @param {number} page - رقم الصفحة الحالية
 * @returns Promise يحتوي على قائمة المنتجات + معلومات الصفحات
 */
export function fetchProducts(filters = {}, page = 0) {
  const params = { page, size: 12, ...filters };
  Object.keys(params).forEach((key) => {
    if (params[key] === "" || params[key] === null || params[key] === undefined) {
      delete params[key];
    }
  });
  return axiosClient.get("/products", { params }).then((res) => res.data);
}

/**
 * تجلب تفاصيل منتج واحد عبر معرّفه.
 * @param {number|string} id - معرّف المنتج
 * @returns Promise يحتوي على بيانات المنتج
 */
export function fetchProduct(id) {
  return axiosClient.get(`/products/${id}`).then((res) => res.data);
}

/**
 * تجلب منتجات ذات صلة (نفس الفئة) لعرضها كـ"قد يعجبك أيضاً".
 * @param {number|string} id - معرّف المنتج الأساسي
 * @returns Promise يحتوي على قائمة منتجات مشابهة
 */
export function fetchRelatedProducts(id) {
  return axiosClient.get(`/products/${id}/related`).then((res) => res.data);
}

/**
 * تجلب كل موديلات مجموعة منتج معيّن (مثل Mini/X/Standard) لعرضها كقائمة اختيار.
 * @param {number|string} id - معرّف أحد موديلات المجموعة
 * @returns Promise يحتوي على قائمة الموديلات (فارغة إذا لم يكن للمنتج موديلات بديلة)
 */
export function fetchProductVariants(id) {
  return axiosClient.get(`/products/${id}/variants`).then((res) => res.data);
}

/**
 * تجلب المراجعات المعتمدة لمنتج معيّن.
 * @param {number|string} id - معرّف المنتج
 * @param {number} page - رقم الصفحة
 * @returns Promise يحتوي على قائمة المراجعات
 */
export function fetchProductReviews(id, page = 0) {
  return axiosClient.get(`/products/${id}/reviews`, { params: { page, size: 10 } }).then((res) => res.data);
}

/**
 * تضيف مراجعة جديدة لمنتج (تتطلب تسجيل دخول).
 * @param {number|string} id - معرّف المنتج
 * @param {{rating:number,comment:string}} payload - التقييم والتعليق
 * @returns Promise يحتوي على بيانات المراجعة المُنشأة
 */
export function addProductReview(id, payload) {
  return axiosClient.post(`/products/${id}/reviews`, payload).then((res) => res.data);
}
