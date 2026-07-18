import { useEffect, useState } from "react";

/**
 * تُرجع نسخة مؤخَّرة (debounced) من قيمة معيّنة، تُستخدم لتقليل عدد نداءات الـ API
 * أثناء كتابة المستخدم بحقل البحث أو تغيير الفلاتر.
 * @param {*} value - القيمة الأصلية المتغيّرة بسرعة
 * @param {number} delayMs - مدة التأخير بالميلي ثانية
 * @returns القيمة بعد التأخير
 */
export function useDebouncedValue(value, delayMs = 400) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return debounced;
}
