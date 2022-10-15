// eslint-disable-next-line import/prefer-default-export
export function extraDomBounding(dom: HTMLElement) {
  const style = getComputedStyle(dom);
  return {
    width: Number(style.width.replace("px", "")),
    height: Number(style.width.replace("px", "")),
  };
}
