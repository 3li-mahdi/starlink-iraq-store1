/**
 * هيكل تحميل مؤقت (skeleton) لبطاقة منتج، يُعرض أثناء انتظار استجابة الـ API.
 */
export default function SkeletonProductCard() {
  return (
    <div className="card" style={{ overflow: "hidden" }}>
      <div className="skeleton" style={{ aspectRatio: "1/1" }} />
      <div style={{ padding: 14, display: "flex", flexDirection: "column", gap: 8 }}>
        <div className="skeleton" style={{ height: 12, width: "40%" }} />
        <div className="skeleton" style={{ height: 16, width: "90%" }} />
        <div className="skeleton" style={{ height: 20, width: "50%" }} />
        <div className="skeleton" style={{ height: 38, width: "100%", marginTop: 6 }} />
      </div>
    </div>
  );
}
