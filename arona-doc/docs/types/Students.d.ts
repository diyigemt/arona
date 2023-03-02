export interface Student {
  id: number;
  familyName: StudentName;
  name: StudentName;
  nickname: string[] | number[];
  club: string;
  affiliation: string;
  rarity: number;
  type: 'Striker' | 'Special';
  armorType: 'LightArmor' | 'HeavyArmor' | 'Unarmed';
  bulletType?: 'Pierce' | 'Explode' | 'Mystic';
  weapon:
    | 'SG'
    | 'SMG'
    | 'AR'
    | 'GL'
    | 'HG'
    | 'RL'
    | 'SR'
    | 'RG'
    | 'MG'
    | 'MT'
    | 'FT';
}

export interface StudentName {
  cn: string;
  jp: string;
  en: string;
}
