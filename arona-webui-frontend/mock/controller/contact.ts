import { Request, Response } from "express";
import { packageResponse } from "../utils";
import { BotContact } from "../../src/interface/http";
import { Group, Permission } from "../../src/types/contact";
// eslint-disable-next-line import/prefer-default-export
export function getContactList(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const data: BotContact = {
        bot: 1355247243,
        groups: [
          {
            id: 1355247243,
            name: "这是假的群",
            permission: "ADMINISTRATOR",
            member: [
              {
                id: 1355247243,
                memberName: "群员1",
                permission: "MEMBER",
                specialTitle: "",
                joinTimestamp: 123,
                lastSpeakTimestamp: 123,
                muteTimeRemaining: 123,
                group: {
                  id: 1355247243,
                  name: "这是假的群",
                  permission: "ADMINISTRATOR",
                },
              },
            ],
          },
        ],
        friends: [
          {
            id: 1355247243,
            name: "这是假的描述",
            remark: "这也是假的备注",
          },
        ],
      };
      resolve(packageResponse(response, data));
    }, 50);
  });
}
