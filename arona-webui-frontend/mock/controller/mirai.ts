import { Request, Response } from "express";
import { packageResponse } from "../utils";
import { Friend, User } from "../../src/types/contact";
// eslint-disable-next-line import/prefer-default-export
export function getBotList(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    const data: Friend[] = [
      {
        id: 12313123,
        name: "测试用机器人",
        remark: "",
      },
      {
        id: 22232233,
        name: "测试用机器人2",
        remark: "",
      },
    ];
    setTimeout(() => {
      resolve(packageResponse(response, data));
    }, 50);
  });
}
