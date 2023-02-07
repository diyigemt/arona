import { Request, Response } from "express";
import { packageResponse } from "../utils";
import {BotContact} from "../../src/interface/modules/contact";
// eslint-disable-next-line import/prefer-default-export
export function getContactList(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const data: BotContact[] = [
        {
          bot: 1355247243,
          groups: [
            {
              id: 1355247243,
              name: "这是假的群",
              permission: "ADMINISTRATOR",
              member: [
                {
                  id: 1355247243,
                  memberName: "这是短的",
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
                {
                  id: 1355247244,
                  memberName: "这是一个很长很长的群员",
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
                {
                  id: 1355247245,
                  memberName: "这也是短的",
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
        },
      ];
      resolve(packageResponse(response, data));
    }, 50);
  });
}
