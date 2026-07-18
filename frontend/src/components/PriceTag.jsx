import { formatIqd } from "../utils/format";

/**
 * يعرض سعر المنتج بخط كبير، مع السعر الأصلي مشطوباً إذا وجد خصم (anchor pricing).
 */
export default function PriceTag({ price, discountPrice }) {
  const hasDiscount = discountPrice != null && discountPrice < price;
  const finalPrice = hasDiscount ? discountPrice : price;

  return (
    <div>
      <span className="price-current">{formatIqd(finalPrice)}</span>
      {hasDiscount && <span className="price-original">{formatIqd(price)}</span>}
    </div>
  );
}
