/**
 * يعرض تقييماً بالنجوم (للعرض فقط، أو تفاعلياً عند تمرير onChange).
 */
export default function StarRating({ value = 0, onChange, size = 16 }) {
  const stars = [1, 2, 3, 4, 5];
  const interactive = typeof onChange === "function";

  return (
    <div style={{ display: "inline-flex", gap: 2 }} role={interactive ? "radiogroup" : undefined} aria-label="التقييم">
      {stars.map((star) => (
        <span
          key={star}
          onClick={interactive ? () => onChange(star) : undefined}
          role={interactive ? "radio" : undefined}
          aria-checked={interactive ? value === star : undefined}
          style={{
            cursor: interactive ? "pointer" : "default",
            color: star <= Math.round(value) ? "#f5a623" : "var(--color-border)",
            fontSize: size,
          }}
        >
          ★
        </span>
      ))}
    </div>
  );
}
