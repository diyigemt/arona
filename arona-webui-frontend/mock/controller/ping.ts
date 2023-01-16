import { Request, Response } from "express";
import { packageResponse } from "../utils";
// eslint-disable-next-line import/prefer-default-export
export function ping(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(packageResponse(response, "pong"));
    }, 50);
  });
}
