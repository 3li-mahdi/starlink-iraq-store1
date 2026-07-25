import { useRef } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import { Stars } from "@react-three/drei";
import * as THREE from "three";

/**
 * كرة أرضية شبكية (wireframe) تدور ببطء، ترمز لتغطية الأقمار الصناعية العالمية لـ Starlink.
 */
function WireframeGlobe() {
  const ref = useRef(null);
  useFrame((_, delta) => {
    if (ref.current) {
      ref.current.rotation.y += delta * 0.12;
    }
  });

  return (
    <mesh ref={ref}>
      <icosahedronGeometry args={[1.8, 3]} />
      <meshBasicMaterial color="#00e5ff" wireframe transparent opacity={0.55} />
    </mesh>
  );
}

/**
 * قمر صناعي واحد يدور حول الأرض بمدار مائل بسرعة ونصف قطر مختلفين لكل قمر.
 * @param {number} radius - نصف قطر المدار
 * @param {number} speed - سرعة الدوران حول الأرض
 * @param {number} tilt - زاوية ميلان المدار بالراديان
 * @param {string} color - لون القمر الصناعي (سيان أو ذهبي)
 */
function OrbitingSatellite({ radius, speed, tilt, color }) {
  const ref = useRef(null);
  const angleRef = useRef(Math.random() * Math.PI * 2);

  useFrame((_, delta) => {
    angleRef.current += delta * speed;
    if (ref.current) {
      const x = Math.cos(angleRef.current) * radius;
      const z = Math.sin(angleRef.current) * radius;
      ref.current.position.set(x, Math.sin(angleRef.current) * radius * Math.sin(tilt), z * Math.cos(tilt));
    }
  });

  return (
    <mesh ref={ref}>
      <octahedronGeometry args={[0.09, 0]} />
      <meshBasicMaterial color={color} />
    </mesh>
  );
}

/**
 * حلقة مدار مرسومة كخط رفيع حول الأرض لإظهار مسار القمر الصناعي بصرياً.
 */
function OrbitRing({ radius, tilt }) {
  const points = [];
  for (let i = 0; i <= 64; i += 1) {
    const angle = (i / 64) * Math.PI * 2;
    points.push(
      new THREE.Vector3(Math.cos(angle) * radius, Math.sin(angle) * radius * Math.sin(tilt), Math.sin(angle) * radius * Math.cos(tilt))
    );
  }
  const geometry = new THREE.BufferGeometry().setFromPoints(points);

  return (
    <line geometry={geometry}>
      <lineBasicMaterial color="#00e5ff" transparent opacity={0.18} />
    </line>
  );
}

const SATELLITES = [
  { radius: 2.6, speed: 0.4, tilt: 0.5, color: "#00e5ff" },
  { radius: 3.1, speed: -0.28, tilt: 1.1, color: "#e8c05f" },
  { radius: 3.6, speed: 0.22, tilt: 0.15, color: "#00e5ff" },
];

/**
 * مشهد Hero الرئيسي: كرة أرضية شبكية بأقمار صناعية تدور حولها بمدارات، فوق حقل نجوم خافت.
 * عنصر بصري خفيف الأداء (لا إضاءة معقّدة ولا نماذج ثقيلة) مناسب كخلفية متحركة أعلى الصفحة الرئيسية.
 */
export default function HeroGlobeScene() {
  return (
    <Canvas camera={{ position: [0, 0.4, 6], fov: 50 }} gl={{ antialias: true, alpha: true }}>
      <Stars radius={60} depth={30} count={1200} factor={2} saturation={0} fade speed={0.4} />
      <WireframeGlobe />
      {SATELLITES.map((sat, index) => (
        <group key={index}>
          <OrbitRing radius={sat.radius} tilt={sat.tilt} />
          <OrbitingSatellite {...sat} />
        </group>
      ))}
    </Canvas>
  );
}
