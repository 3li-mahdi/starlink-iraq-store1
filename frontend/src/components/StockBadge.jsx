/**
 * يعرض شارة "متبقي X قطع فقط" عند انخفاض المخزون لخلق إلحاح شراء نفسي،
 * أو شارة "نفذت الكمية" إذا كان المخزون صفراً.
 */
export default function StockBadge({ stockQuantity, lowStock, requiresShipping }) {
  if (!requiresShipping) {
    return null;
  }
  if (stockQuantity === 0) {
    return <span className="badge badge-muted">نفذت الكمية</span>;
  }
  if (lowStock) {
    return <span className="badge badge-danger">متبقي {stockQuantity} قطع فقط!</span>;
  }
  return null;
}
