/**
 * تُرجع شكلاً هندسياً ثلاثي الأبعاد بسيطاً (بديل مؤقت لحين توفر نماذج GLB حقيقية للمنتجات)،
 * يُختار حسب فئة المنتج ونوعه (رقمي/مادي) لإعطاء تنوّع بصري بسيط.
 *
 * @param {object} product - بيانات المنتج (يُستخدم category وproductType لاختيار الشكل واللون)
 * @param {number} rotationSpeed - سرعة الدوران التلقائي حول المحور y
 * @returns عنصر mesh ثلاثي الأبعاد جاهز للعرض داخل Canvas
 */
export default function ProductGeometry({ product, groupRef }) {
  const isDigital = product?.productType === "DIGITAL";
  const color = isDigital ? "#e8c05f" : "#00e5ff";
  const category = (product?.category || "").toLowerCase();

  let geometry = <icosahedronGeometry args={[1.3, 0]} />;
  if (category.includes("راوتر") || category.includes("شبك")) {
    geometry = <boxGeometry args={[1.7, 1.7, 1.7]} />;
  } else if (category.includes("طبق")) {
    geometry = <coneGeometry args={[1.4, 1.6, 5]} />;
  } else if (category.includes("كيبل")) {
    geometry = <torusKnotGeometry args={[0.9, 0.28, 100, 12]} />;
  } else if (isDigital || category.includes("اشتراك")) {
    geometry = <octahedronGeometry args={[1.5, 0]} />;
  }

  return (
    <group ref={groupRef}>
      <mesh>
        {geometry}
        <meshBasicMaterial color={color} wireframe transparent opacity={0.9} />
      </mesh>
      <mesh scale={0.985}>
        {geometry}
        <meshBasicMaterial color={color} transparent opacity={0.06} />
      </mesh>
    </group>
  );
}
