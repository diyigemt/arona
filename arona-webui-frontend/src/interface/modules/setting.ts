import { Config2Form } from "..";

export interface APISetting {
  host: string;
  port: number;
}

export type APISettingForm = Config2Form<APISetting>;
