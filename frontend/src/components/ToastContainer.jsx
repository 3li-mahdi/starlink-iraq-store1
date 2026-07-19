import { useDispatch, useSelector } from "react-redux";
import { AnimatePresence, motion } from "framer-motion";
import { dismissToast } from "../features/ui/uiSlice";
import { useEffect } from "react";

const TYPE_ICONS = {
  success: "✓",
  error: "✕",
  info: "ℹ",
};

/**
 * عنصر واحد من الإشعارات المؤقتة (toast)، يختفي تلقائياً بعد بضع ثوانٍ.
 * التمييز بين الأنواع يتم عبر الأيقونة فقط، بدون ألوان زاهية، حفاظاً على الثيم الأحادي.
 */
function Toast({ toast, onDismiss }) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 3500);
    return () => clearTimeout(timer);
  }, [onDismiss]);

  return (
    <motion.div
      initial={{ opacity: 0, y: -12, scale: 0.97 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -8, scale: 0.97 }}
      transition={{ duration: 0.18 }}
      role="status"
      className="card"
      style={{
        display: "flex",
        alignItems: "center",
        gap: 10,
        boxShadow: "var(--shadow-elevated)",
        padding: "12px 16px",
        minWidth: 260,
        color: "var(--color-text)",
        fontSize: 14,
        fontWeight: 600,
        cursor: "pointer",
      }}
      onClick={onDismiss}
    >
      <span aria-hidden style={{ fontWeight: 800 }}>
        {TYPE_ICONS[toast.type] || TYPE_ICONS.info}
      </span>
      {toast.message}
    </motion.div>
  );
}

/**
 * حاوية تعرض كل الإشعارات النشطة أعلى الصفحة.
 */
export default function ToastContainer() {
  const toasts = useSelector((state) => state.ui.toasts);
  const dispatch = useDispatch();

  return (
    <div style={{ position: "fixed", top: 16, insetInlineEnd: 16, zIndex: 999, display: "flex", flexDirection: "column", gap: 10 }}>
      <AnimatePresence>
        {toasts.map((toast) => (
          <Toast key={toast.id} toast={toast} onDismiss={() => dispatch(dismissToast(toast.id))} />
        ))}
      </AnimatePresence>
    </div>
  );
}
