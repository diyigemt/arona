import { Request, Response } from "express";
import { packageResponse } from "../utils";
// eslint-disable-next-line import/prefer-default-export
export function getContactList(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(
        packageResponse(response, {
          bot: 1,
          groups: [],
          friends: [],
        }),
      );
    }, 50);
  });
}
