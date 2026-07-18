import reducer, { loadWishlist, selectIsInWishlist } from "../features/wishlist/wishlistSlice";

const sampleWishlist = [
  { id: 1, product: { id: 10, name: "طبق Starlink" }, addedAt: "2026-01-01T00:00:00" },
];

describe("wishlistSlice", () => {
  test("loadWishlist.fulfilled يملأ قائمة الرغبات", () => {
    const state = reducer(undefined, { type: loadWishlist.fulfilled.type, payload: sampleWishlist });
    expect(state.items).toHaveLength(1);
    expect(state.status).toBe("succeeded");
  });

  test("selectIsInWishlist يرجع true للمنتج الموجود بالقائمة", () => {
    const wishlistState = reducer(undefined, { type: loadWishlist.fulfilled.type, payload: sampleWishlist });
    const state = { wishlist: wishlistState };
    expect(selectIsInWishlist(10)(state)).toBe(true);
    expect(selectIsInWishlist(999)(state)).toBe(false);
  });
});
