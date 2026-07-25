import { useRef } from "react";
import { Link } from "react-router-dom";
import { motion, useMotionValue, useSpring, useTransform } from "framer-motion";

/**
 * بطاقة فئة تميل بشكل ثلاثي الأبعاد (3D tilt) تبعاً لموضع الماوس فوقها، وتنقل المستخدم
 * لصفحة المتجر مفلترة على هذه الفئة عند الضغط عليها.
 * @param {{icon:string,title:string,description:string,category:string}} props - بيانات الفئة
 */
export default function CategoryTiltCard({ icon, title, description, category }) {
  const cardRef = useRef(null);
  const mouseX = useMotionValue(0);
  const mouseY = useMotionValue(0);

  const rotateX = useSpring(useTransform(mouseY, [-0.5, 0.5], [10, -10]), { stiffness: 200, damping: 20 });
  const rotateY = useSpring(useTransform(mouseX, [-0.5, 0.5], [-10, 10]), { stiffness: 200, damping: 20 });

  function handleMouseMove(event) {
    const rect = cardRef.current.getBoundingClientRect();
    mouseX.set((event.clientX - rect.left) / rect.width - 0.5);
    mouseY.set((event.clientY - rect.top) / rect.height - 0.5);
  }

  function handleMouseLeave() {
    mouseX.set(0);
    mouseY.set(0);
  }

  return (
    <Link to={`/products?category=${encodeURIComponent(category)}`} style={{ display: "block" }}>
      <motion.div
        ref={cardRef}
        onMouseMove={handleMouseMove}
        onMouseLeave={handleMouseLeave}
        style={{ rotateX, rotateY, transformPerspective: 800 }}
        className="card"
        whileHover={{ scale: 1.02 }}
        transition={{ duration: 0.2 }}
      >
        <div style={{ padding: 32, textAlign: "center" }}>
          <div style={{ fontSize: 40, marginBottom: 14 }} aria-hidden>
            {icon}
          </div>
          <h3 style={{ margin: "0 0 8px" }}>{title}</h3>
          <p style={{ color: "var(--color-text-muted)", margin: 0, fontSize: 14 }}>{description}</p>
        </div>
      </motion.div>
    </Link>
  );
}
