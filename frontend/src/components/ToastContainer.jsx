import { useDispatch, useSelector } from "react-redux";
import { AnimatePresence, motion } from "framer-motion";
import { dismissToast } from "../features/ui/uiSlice";
import { useEffect } from "react";

const TYPE_COLORS = {
  success: "var(--color-success)",
  error: "var(--color-danger)",
  info: "var(--color-primary)",
};

/**
 * عنصر واحد من الإشعارات المؤقتة (toast)، يختفي تلقائياً بعد بضع ثوانٍ.
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
      style={{
        backgroundColor: "var(--color-surface)",
        borderInlineStart: `4px solid ${TYPE_COLORS[toast.type] || TYPE_COLORS.info}`,
        boxShadow: "var(--shadow-elevated)",
        borderRadius: "var(--radius-md)",
        padding: "12px 16px",
        minWidth: 260,
        color: "var(--color-text)",
        fontSize: 14,
        fontWeight: 600,
      }}
      onClick={onDismiss}
    >
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
