import { transformVoicedOrSemiVoiced } from './CJKService';

function matchElementByHash(
  decodedHash: string,
  elements: NodeListOf<Element>
): HTMLElement | undefined {
  const transformedHash = transformVoicedOrSemiVoiced(decodedHash);
  const elementList = Array.from(elements).filter(
    element => transformedHash === element.id
  );

  return elementList.length > 0 ? (elementList[0] as HTMLElement) : undefined;
}

export { matchElementByHash };
