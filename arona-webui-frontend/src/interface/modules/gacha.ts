export interface GachaCharacter {
  id: number;
  name: string;
  star: number;
  limit: boolean;
}

export interface GachaPool {
  id: number;
  name: string;
  characters: GachaCharacter[];
}