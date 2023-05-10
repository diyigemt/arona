export type ImageFuzzySearchCacheItem = {
  id: number;
  name: string;
  recommendScore: number;
};

export type ImageItem = {
  name: string;
  path: string;
  hash: string;
  type: number;
};
