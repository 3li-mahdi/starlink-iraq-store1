import { useState } from "react";
import { Link } from "react-router-dom";
import Breadcrumbs from "../components/Breadcrumbs";

const FAQ_ITEMS = [
  {
    question: "كم مدة توصيل الطلب؟",
    answer: "عادةً يصل طلبك خلال 2-5 أيام عمل داخل العراق حسب المحافظة.",
  },
  {
    question: "هل أقدر أرجع منتج اشتريته؟",
    answer: "نعم، يمكنك إرجاع أي منتج مادي خلال 7 أيام من الاستلام إذا كان بحالته الأصلية.",
  },
  {
    question: "كيف أستلم المنتجات الرقمية (أكواد التفعيل)؟",
    answer: "تظهر أكواد التفعيل أو روابط التحميل مباشرة بصفحة تأكيد الطلب وبصفحة \"طلباتي\" فور إتمام الدفع.",
  },
  {
    question: "شنو طرق الدفع المتوفرة؟",
    answer: "حالياً الدفع عبر بوابة الدفع الإلكتروني بالموقع، وقريباً راح نضيف ZainCash.",
  },
  {
    question: "كيف أتابع حالة طلبي؟",
    answer: "من صفحة \"طلباتي\" بعد تسجيل الدخول، تكدر تشوف حالة كل طلب لحظياً.",
  },
];

/**
 * صفحة "خدمة العملاء": أسئلة شائعة قابلة للطي + رابط للتواصل المباشر.
 */
export default function HelpCenterPage() {
  const [openIndex, setOpenIndex] = useState(null);

  return (
    <div style={{ maxWidth: 720, margin: "24px auto", padding: "0 16px" }}>
      <Breadcrumbs items={[{ label: "خدمة العملاء" }]} />
      <h1>خدمة العملاء</h1>
      <p style={{ color: "var(--color-text-muted)" }}>إليك إجابات لأكثر الأسئلة شيوعاً.</p>

      <div style={{ display: "flex", flexDirection: "column", gap: 10, marginTop: 20 }}>
        {FAQ_ITEMS.map((item, index) => {
          const isOpen = openIndex === index;
          return (
            <div key={item.question} className="card" style={{ overflow: "hidden" }}>
              <button
                onClick={() => setOpenIndex(isOpen ? null : index)}
                aria-expanded={isOpen}
                style={{
                  width: "100%",
                  textAlign: "start",
                  background: "transparent",
                  border: "none",
                  color: "var(--color-text)",
                  padding: "16px 18px",
                  fontSize: 15,
                  fontWeight: 700,
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  gap: 12,
                }}
              >
                {item.question}
                <span aria-hidden style={{ transition: "transform 0.2s ease", transform: isOpen ? "rotate(180deg)" : "none" }}>
                  ⌄
                </span>
              </button>
              {isOpen && (
                <p style={{ padding: "0 18px 16px", margin: 0, color: "var(--color-text-muted)", lineHeight: 1.8 }}>{item.answer}</p>
              )}
            </div>
          );
        })}
      </div>

      <div className="card" style={{ padding: 20, marginTop: 24, textAlign: "center" }}>
        <p style={{ marginTop: 0 }}>ما لقيت جواب لسؤالك؟</p>
        <Link to="/contact" className="btn btn-outline">
          تواصل معنا مباشرة
        </Link>
      </div>
    </div>
  );
}
