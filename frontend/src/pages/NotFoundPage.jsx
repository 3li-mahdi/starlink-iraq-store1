import { Link } from "react-router-dom";

/**
 * صفحة تُعرض عند طلب مسار غير موجود بالتطبيق.
 */
export default function NotFoundPage() {
  return (
    <div style={{ textAlign: "center", padding: "80px 0" }}>
      <h1>404</h1>
      <p>الصفحة التي تبحث عنها غير موجودة.</p>
      <Link to="/" className="btn btn-cta">
        العودة للرئيسية
      </Link>
    </div>
  );
}
