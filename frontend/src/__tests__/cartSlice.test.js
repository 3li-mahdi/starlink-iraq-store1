import reducer, { clearCartState, selectCartItemCount, loadCart, addItemToCart } from "../features/cart/cartSlice";

const sampleCartResponse = {
  id: 5,
  items: [
    { id: 1, productId: 10, productName: "طبق Starlink", unitPrice: 900, quantity: 2, subtotal: 1800, availableStock: 10 },
    { id: 2, productId: 11, productName: "راوتر", unitPrice: 400, quantity: 1, subtotal: 400, availableStock: 5 },
  ],
  totalAmount: 2200,
};

describe("cartSlice reducer", () => {
  test("الحالة الابتدائية سلة فارغة", () => {
    const state = reducer(undefined, { type: "@@INIT" });
    expect(state.items).toEqual([]);
    expect(state.totalAmount).toBe(0);
  });

  test("loadCart.fulfilled يملأ عناصر السلة والسعر الإجمالي", () => {
    const state = reducer(undefined, { type: loadCart.fulfilled.type, payload: sampleCartResponse });
    expect(state.items).toHaveLength(2);
    expect(state.totalAmount).toBe(2200);
    expect(state.status).toBe("succeeded");
  });

  test("addItemToCart.fulfilled يحدّث السلة بالكامل من استجابة السيرفر", () => {
    const state = reducer(undefined, { type: addItemToCart.fulfilled.type, payload: sampleCartResponse });
    expect(state.items[0].productName).toBe("طبق Starlink");
  });

  test("clearCartState يفرّغ السلة محلياً بعد إتمام الشراء", () => {
    const filledState = reducer(undefined, { type: loadCart.fulfilled.type, payload: sampleCartResponse });
    const clearedState = reducer(filledState, clearCartState());
    expect(clearedState.items).toEqual([]);
    expect(clearedState.totalAmount).toBe(0);
  });

  test("selectCartItemCount يجمع كميات كل العناصر", () => {
    const state = { cart: reducer(undefined, { type: loadCart.fulfilled.type, payload: sampleCartResponse }) };
    expect(selectCartItemCount(state)).toBe(3);
  });
});
