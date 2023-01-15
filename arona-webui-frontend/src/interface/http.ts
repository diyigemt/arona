import { Friend, Group } from "@/types/contact";

export interface BotContact {
  bot: number;
  groups: Group[];
  friends: Friend[];
}
