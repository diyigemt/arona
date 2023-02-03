import { Request, Response } from "express";
import { packageResponse } from "../utils";

// eslint-disable-next-line import/prefer-default-export
export function uploadImage(request: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    // @ts-ignore
    if (request.files) {
      // @ts-ignore
      console.log(request.files);
    }
    resolve(packageResponse(response, "123"));
  });
}
