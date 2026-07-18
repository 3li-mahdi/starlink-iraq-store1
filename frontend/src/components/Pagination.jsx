/**
 * أزرار تنقّل بسيطة بين صفحات نتائج مُرقّمة (paginated).
 */
export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div style={{ display: "flex", justifyContent: "center", gap: 8, marginTop: 24 }}>
      <button className="btn btn-outline" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        السابق
      </button>
      <span style={{ display: "flex", alignItems: "center", fontSize: 14, color: "var(--color-text-muted)" }}>
        صفحة {page + 1} من {totalPages}
      </span>
      <button className="btn btn-outline" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        التالي
      </button>
    </div>
  );
}
