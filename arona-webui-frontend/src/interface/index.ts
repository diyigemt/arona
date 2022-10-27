export * from "./modules/config";
export type Config2Map<Config> = {
  [key in keyof Config]: {
    value: Config[key];
    description: string;
  };
};

export type Config2Form<Config> = {
  [key in keyof Config]: Config[key] | null;
};
