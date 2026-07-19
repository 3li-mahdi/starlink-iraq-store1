import { useState } from "react";
import { useDispatch } from "react-redux";
import Breadcrumbs from "../components/Breadcrumbs";
import { showToast } from "../features/ui/uiSlice";

const EMPTY_FORM = { name: "", email: "", message: "" };

/**
 * صفحة "تواصل معنا": فورم بسيط لإرسال استفسار (اسم، إيميل، رسالة).
 */
export default function ContactPage() {
  const dispatch = useDispatch();
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);

  function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    // ملاحظة: لا يوجد endpoint مخصص بالـ backend لاستقبال رسائل التواصل حالياً،
    // لذا تُعرض رسالة تأكيد محلية فقط. يمكن ربطها لاحقاً بخدمة بريد أو endpoint مخصص.
    setTimeout(() => {
      dispatch(showToast("تم استلام رسالتك، سنتواصل معك قريباً", "success"));
      setForm(EMPTY_FORM);
      setSubmitting(false);
    }, 500);
  }

  return (
    <div style={{ maxWidth: 560, margin: "24px auto", padding: "0 16px" }}>
      <Breadcrumbs items={[{ label: "تواصل معنا" }]} />
      <h1>تواصل معنا</h1>
      <p style={{ color: "var(--color-text-muted)" }}>
        عندك سؤال أو استفسار؟ راسلنا وراح نرد عليك بأسرع وقت.
      </p>

      <form onSubmit={handleSubmit} className="card" style={{ padding: 24, marginTop: 20 }}>
        <div className="form-field">
          <label htmlFor="name">الاسم</label>
          <input id="name" required value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} />
        </div>
        <div className="form-field">
          <label htmlFor="email">البريد الإلكتروني</label>
          <input
            id="email"
            type="email"
            required
            value={form.email}
            onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
          />
        </div>
        <div className="form-field">
          <label htmlFor="message">رسالتك</label>
          <textarea
            id="message"
            rows={5}
            required
            value={form.message}
            onChange={(e) => setForm((f) => ({ ...f, message: e.target.value }))}
          />
        </div>
        <button className="btn btn-cta" style={{ width: "100%" }} type="submit" disabled={submitting}>
          {submitting ? "...جارٍ الإرسال" : "إرسال الرسالة"}
        </button>
      </form>
    </div>
  );
}
