import { render, screen } from "@testing-library/react";
import PriceTag from "../components/PriceTag";

describe("PriceTag", () => {
  test("يعرض السعر فقط عند عدم وجود خصم", () => {
    render(<PriceTag price={1000} discountPrice={null} />);
    expect(screen.getByText(/1,000/)).toBeInTheDocument();
    expect(screen.queryByText("price-original")).not.toBeInTheDocument();
  });

  test("يعرض السعر الأصلي مشطوباً والسعر الجديد عند وجود خصم", () => {
    const { container } = render(<PriceTag price={1000} discountPrice={750} />);
    expect(screen.getByText(/750/)).toBeInTheDocument();
    expect(container.querySelector(".price-original")).toHaveTextContent("1,000");
  });
});
