import { Request, Response } from "express";
import { packageResponse } from "../utils";
import { ReplyGroup, ReplyItemType, ReplyLabel } from "../../src/interface/modules/reply";

export function getReplyGroup(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const data: ReplyGroup[] = [
        {
          id: 1,
          content: [{ type: "String", content: "测试1" }],
          weight: 1,
          label: [1],
        },
        {
          id: 2,
          content: [
            { type: "String", content: "测试2-1" },
            { type: "Image", content: "测试2-2" },
          ],
          weight: 1,
          label: [2],
        },
        {
          id: 3,
          content: [{ type: "String", content: "测试3" }],
          weight: 2,
          label: [1, 2],
        },
      ];
      resolve(packageResponse(response, data));
    }, 50);
  });
}

export function getReplyLabel(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const data: ReplyLabel[] = [
        {
          id: 1,
          value: "标签1",
          weight: 1,
        },
        {
          id: 2,
          value: "标签2",
          weight: 2,
        },
      ];
      resolve(packageResponse(response, data));
    }, 50);
  });
}
