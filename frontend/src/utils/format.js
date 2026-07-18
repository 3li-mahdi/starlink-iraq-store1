/**
 * تُنسّق مبلغاً مالياً بالدينار العراقي بأرقام إنجليزية (لضمان وضوح القراءة) مع فواصل الآلاف.
 * @param {number} value - المبلغ المطلوب تنسيقه
 * @returns {string} نص المبلغ منسَّقاً مع "د.ع"
 */
export function formatIqd(value) {
  const formatted = Number(value).toLocaleString("en-US", { maximumFractionDigits: 2 });
  return `${formatted} د.ع`;
}
