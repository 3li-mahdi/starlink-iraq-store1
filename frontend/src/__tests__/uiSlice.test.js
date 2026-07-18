import reducer, { showToast, dismissToast } from "../features/ui/uiSlice";

describe("uiSlice", () => {
  test("showToast يضيف إشعاراً جديداً للحالة", () => {
    const state = reducer(undefined, showToast("تم الحفظ", "success"));
    expect(state.toasts).toHaveLength(1);
    expect(state.toasts[0].message).toBe("تم الحفظ");
    expect(state.toasts[0].type).toBe("success");
  });

  test("dismissToast يحذف الإشعار المحدَّد فقط", () => {
    let state = reducer(undefined, showToast("رسالة 1"));
    state = reducer(state, showToast("رسالة 2"));
    const idToRemove = state.toasts[0].id;

    state = reducer(state, dismissToast(idToRemove));

    expect(state.toasts).toHaveLength(1);
    expect(state.toasts[0].message).toBe("رسالة 2");
  });
});
