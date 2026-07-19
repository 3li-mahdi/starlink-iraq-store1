import { Link } from "react-router-dom";

const COMPANY_LINKS = [
  { to: "/products", label: "المتجر" },
  { to: "/help", label: "خدمة العملاء" },
  { to: "/contact", label: "تواصل معنا" },
];

const SOCIAL_LINKS = [
  { href: "https://facebook.com", label: "فيسبوك", icon: "📘" },
  { href: "https://instagram.com", label: "إنستغرام", icon: "📷" },
  { href: "https://twitter.com", label: "إكس", icon: "✖️" },
];

/**
 * تذييل الصفحة: روابط الشركة، حسابات السوشال ميديا، شارات الثقة، وحقوق النشر.
 */
export default function Footer() {
  return (
    <footer style={{ borderTop: "1px solid var(--color-border)", marginTop: 60 }}>
      <div
        className="container"
        style={{ padding: "36px 20px", display: "flex", flexWrap: "wrap", gap: 32, justifyContent: "space-between" }}
      >
        <div style={{ flex: "1 1 240px" }}>
          <strong>متجر Starlink العراق</strong>
          <p style={{ color: "var(--color-text-muted)", fontSize: 14, maxWidth: 320 }}>
            وجهتك الموثوقة لمنتجات Starlink في العراق: أطباق، راوترات، كيبلات، واشتراكات رقمية.
          </p>
          <div style={{ display: "flex", gap: 14, marginTop: 12 }}>
            {SOCIAL_LINKS.map((social) => (
              <a
                key={social.label}
                href={social.href}
                target="_blank"
                rel="noreferrer"
                aria-label={social.label}
                className="icon-btn"
                style={{ fontSize: 18 }}
              >
                {social.icon}
              </a>
            ))}
          </div>
        </div>

        <nav style={{ flex: "1 1 160px" }}>
          <strong style={{ fontSize: 14 }}>روابط الشركة</strong>
          <ul style={{ listStyle: "none", padding: 0, margin: "10px 0 0", display: "flex", flexDirection: "column", gap: 8 }}>
            {COMPANY_LINKS.map((link) => (
              <li key={link.to}>
                <Link to={link.to} style={{ fontSize: 14, color: "var(--color-text-muted)" }}>
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>

        <div style={{ flex: "1 1 200px", display: "flex", flexDirection: "column", gap: 8, fontSize: 13, color: "var(--color-text-muted)" }}>
          <span>🛡️ ضمان على كل المنتجات</span>
          <span>🔒 دفع آمن 100%</span>
          <span>↩️ إرجاع خلال 7 أيام</span>
        </div>
      </div>
      <div
        style={{
          textAlign: "center",
          padding: "12px",
          fontSize: 12,
          color: "var(--color-text-muted)",
          borderTop: "1px solid var(--color-border)",
        }}
      >
        © {new Date().getFullYear()} متجر Starlink العراق. جميع الحقوق محفوظة.
      </div>
    </footer>
  );
}
