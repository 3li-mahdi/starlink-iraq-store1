import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ProductCard from "../components/ProductCard";
import SkeletonProductCard from "../components/SkeletonProductCard";
import Pagination from "../components/Pagination";
import Breadcrumbs from "../components/Breadcrumbs";
import { fetchProducts } from "../api/productsApi";
import { useDebouncedValue } from "../hooks/useDebouncedValue";

const CATEGORIES = ["", "أطباق", "راوترات", "كيبلات", "اشتراكات"];

const inputStyle = {
  padding: "10px 14px",
  borderRadius: "var(--radius-md)",
  border: "1.5px solid var(--color-border-light)",
  backgroundColor: "var(--color-surface)",
  color: "var(--color-text)",
};

/**
 * صفحة المتجر: بحث وفلترة ديناميكية (فئة، سعر، نوع) وصفحات نتائج، دون إعادة تحميل الصفحة.
 */
export default function ProductListPage() {
  const [searchParams] = useSearchParams();
  const [search, setSearch] = useState(searchParams.get("search") || "");
  const [category, setCategory] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [productType, setProductType] = useState("");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);

  const debouncedSearch = useDebouncedValue(search, 400);
  const debouncedMinPrice = useDebouncedValue(minPrice, 400);
  const debouncedMaxPrice = useDebouncedValue(maxPrice, 400);

  useEffect(() => {
    setPage(0);
  }, [debouncedSearch, category, debouncedMinPrice, debouncedMaxPrice, productType]);

  useEffect(() => {
    setLoading(true);
    fetchProducts(
      {
        search: debouncedSearch,
        category,
        minPrice: debouncedMinPrice,
        maxPrice: debouncedMaxPrice,
        productType,
      },
      page
    )
      .then(setResult)
      .finally(() => setLoading(false));
  }, [debouncedSearch, category, debouncedMinPrice, debouncedMaxPrice, productType, page]);

  return (
    <div style={{ padding: "24px 0" }}>
      <Breadcrumbs items={[{ label: "المتجر" }]} />
      <h1 style={{ marginBottom: 20 }}>المتجر</h1>

      <div className="card" style={{ padding: 16, marginBottom: 24, display: "flex", flexWrap: "wrap", gap: 12 }}>
        <input
          type="search"
          placeholder="ابحث عن منتج..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{ ...inputStyle, flex: "1 1 220px" }}
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)} style={inputStyle}>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>
              {c || "كل الفئات"}
            </option>
          ))}
        </select>
        <select value={productType} onChange={(e) => setProductType(e.target.value)} style={inputStyle}>
          <option value="">كل الأنواع</option>
          <option value="PHYSICAL">مادي</option>
          <option value="DIGITAL">رقمي</option>
        </select>
        <input
          type="number"
          placeholder="أقل سعر"
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
          style={{ ...inputStyle, width: 110 }}
        />
        <input
          type="number"
          placeholder="أعلى سعر"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
          style={{ ...inputStyle, width: 110 }}
        />
      </div>

      <div className="grid-products">
        {loading || !result
          ? Array.from({ length: 8 }).map((_, i) => <SkeletonProductCard key={i} />)
          : result.content.map((product) => <ProductCard key={product.id} product={product} />)}
      </div>

      {!loading && result && result.content.length === 0 && (
        <div style={{ textAlign: "center", padding: "60px 0", color: "var(--color-text-muted)" }}>
          <p style={{ fontSize: 32 }}>🔎</p>
          <p>لا توجد منتجات مطابقة لبحثك.</p>
        </div>
      )}

      {result && <Pagination page={result.page} totalPages={result.totalPages} onChange={setPage} />}
    </div>
  );
}
