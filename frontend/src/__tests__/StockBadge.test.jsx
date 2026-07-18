import { render, screen } from "@testing-library/react";
import StockBadge from "../components/StockBadge";

describe("StockBadge", () => {
  test("يعرض تنبيه المخزون المنخفض عندما lowStock = true", () => {
    render(<StockBadge stockQuantity={3} lowStock={true} requiresShipping={true} />);
    expect(screen.getByText(/متبقي 3 قطع فقط/)).toBeInTheDocument();
  });

  test("يعرض 'نفذت الكمية' عندما تكون الكمية صفراً", () => {
    render(<StockBadge stockQuantity={0} lowStock={false} requiresShipping={true} />);
    expect(screen.getByText("نفذت الكمية")).toBeInTheDocument();
  });

  test("لا يعرض أي شارة عندما يكون المخزون كافياً", () => {
    const { container } = render(<StockBadge stockQuantity={50} lowStock={false} requiresShipping={true} />);
    expect(container).toBeEmptyDOMElement();
  });

  test("لا يعرض أي شارة للمنتجات الرقمية التي لا تحتاج شحناً", () => {
    const { container } = render(<StockBadge stockQuantity={0} lowStock={false} requiresShipping={false} />);
    expect(container).toBeEmptyDOMElement();
  });
});
