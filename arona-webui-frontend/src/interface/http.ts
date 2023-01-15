import { Friend, Group, Member } from "@/types/contact";

export interface ExtendGroup extends Group {
  member: Member[];
}

export interface BotContact {
  bot: number;
  groups: ExtendGroup[];
  friends: Friend[];
}
