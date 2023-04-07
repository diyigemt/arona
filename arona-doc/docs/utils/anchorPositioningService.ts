import { matchElementByHash } from './hashMatchingService';

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

export function scrollElementIntoViewAfterMounted() {
  const hash = decodeURIComponent(window.location.hash).slice(1);
  if (hash) {
    const elements = document.querySelectorAll("h2, h3, h4, h5, h6");
    const element = matchElementByHash(hash, elements);
    if (element) {
      scrollElementIntoView(element);
    }
  }
}

export { scrollElementIntoView, isInViewport };
