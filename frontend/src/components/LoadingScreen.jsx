import { lazy, Suspense } from "react";
import { AnimatePresence, motion } from "framer-motion";

const LoadingLogo3D = lazy(() => import("./LoadingLogo3D"));

/**
 * شاشة تحميل أولية بشعار 3D دوّار، تُعرض فوق باقي التطبيق لحين اكتمال استعادة جلسة المستخدم،
 * وتختفي تدريجياً بعد ذلك دون التأثير على تحميل باقي الصفحة.
 * @param {{show: boolean}} props - إظهار/إخفاء شاشة التحميل
 */
export default function LoadingScreen({ show }) {
  return (
    <AnimatePresence>
      {show && (
        <motion.div
          initial={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.4 }}
          style={{
            position: "fixed",
            inset: 0,
            zIndex: 500,
            backgroundColor: "var(--color-bg)",
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            gap: 16,
          }}
        >
          <div style={{ width: 140, height: 140 }}>
            <Suspense fallback={null}>
              <LoadingLogo3D />
            </Suspense>
          </div>
          <strong style={{ color: "var(--color-cyan)", letterSpacing: 1 }}>متجر Starlink العراق</strong>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
