function isInViewport(el: HTMLElement) {
  const rect = el.getBoundingClientRect();
  return (
    rect.top >= 0 &&
    rect.left >= 0 &&
    rect.bottom <=
      (window.innerHeight || document.documentElement.clientHeight) &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  );
}

function scrollElementIntoView(element: HTMLElement, immediate = true) {
  const offsetTime = immediate ? 0 : 500;
  setTimeout(() => {
    if (!isInViewport(element)) {
      const top = element.offsetTop;

      window.scrollTo({
        top: top,
      });
    }
  }, offsetTime);
}

export { scrollElementIntoView, isInViewport };
