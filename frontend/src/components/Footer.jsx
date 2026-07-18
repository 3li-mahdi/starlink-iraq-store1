/**
 * تذييل الصفحة: شارات الثقة (ضمان، دفع آمن، إرجاع) ومعلومات عامة عن المتجر.
 */
export default function Footer() {
  return (
    <footer style={{ backgroundColor: "var(--color-primary-dark)", color: "#fff", marginTop: 60 }}>
      <div className="container" style={{ padding: "32px 20px", display: "flex", flexWrap: "wrap", gap: 24, justifyContent: "space-between" }}>
        <div>
          <strong>متجر Starlink العراق</strong>
          <p style={{ color: "rgba(255,255,255,0.7)", fontSize: 14, maxWidth: 320 }}>
            وجهتك الموثوقة لمنتجات Starlink في العراق: أطباق، راوترات، كيبلات، واشتراكات رقمية.
          </p>
        </div>
        <div style={{ display: "flex", gap: 20, flexWrap: "wrap", fontSize: 14, color: "rgba(255,255,255,0.85)" }}>
          <span>🛡️ ضمان على كل المنتجات</span>
          <span>🔒 دفع آمن 100%</span>
          <span>↩️ إرجاع خلال 7 أيام</span>
        </div>
      </div>
      <div style={{ textAlign: "center", padding: "12px", fontSize: 12, color: "rgba(255,255,255,0.5)", borderTop: "1px solid rgba(255,255,255,0.1)" }}>
        © {new Date().getFullYear()} متجر Starlink العراق. جميع الحقوق محفوظة.
      </div>
    </footer>
  );
}
