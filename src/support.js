import { useEffect, useRef, useState } from "react";

export function useScrollReveal(threshold = 0.15) {
  const ref = useRef(null);
  const [visible, setVisible] = useState(false);
  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          observer.disconnect();
        }
      },
      { threshold },
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [threshold]);
  return [ref, visible];
}

export function useLiveActivity() {
  const [count, setCount] = useState(() => 8 + Math.floor(Math.random() * 15));
  useEffect(() => {
    const id = setInterval(() => {
      setCount((prev) => Math.max(3, prev + Math.floor(Math.random() * 5) - 2));
    }, 6000);
    return () => clearInterval(id);
  }, []);
  return count;
}
