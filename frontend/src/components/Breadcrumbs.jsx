import { Link } from "react-router-dom";

/**
 * مسار تنقّل (الرئيسية > القسم > الصفحة الحالية) لتسهيل التنقّل ومعرفة موقع المستخدم بالموقع.
 * @param {{items: Array<{label: string, to?: string}>}} props - قائمة العناصر بعد "الرئيسية"،
 * كل عنصر بدون "to" يُعرض كنص نهائي غير قابل للنقر (الصفحة الحالية).
 */
export default function Breadcrumbs({ items }) {
  return (
    <nav aria-label="مسار التنقل" style={{ marginBottom: 18, fontSize: 13, color: "var(--color-text-muted)" }}>
      <Link to="/" style={{ color: "var(--color-text-muted)" }}>
        الرئيسية
      </Link>
      {items.map((item, index) => (
        <span key={index}>
          <span style={{ margin: "0 6px" }} aria-hidden>
            /
          </span>
          {item.to ? (
            <Link to={item.to} style={{ color: "var(--color-text-muted)" }}>
              {item.label}
            </Link>
          ) : (
            <span style={{ color: "var(--color-text)" }}>{item.label}</span>
          )}
        </span>
      ))}
    </nav>
  );
}
