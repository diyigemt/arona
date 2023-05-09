import { Request, Response } from "express";
import {
  packageEmptyResponse,
  packageErrorResponse,
  packageRawErrorResponse,
  packageResponse,
} from "../../../utils";
import { HttpCode } from "../../../types";
import Prisma from "../../../../db";
import { ImageItem } from "../../../../types";

export async function queryImage(request: Request, response: Response) {
  const name = request.query.name;
  if (typeof name !== "string" || name === "") {
    return packageErrorResponse(
      response,
      HttpCode.BAD_REQUEST,
      "name is empty",
    );
  }
  return new Promise<Response>((resolve) => {
    Prisma.image
      .findFirst({
        where: {
          name,
        },
      })
      .then((image) => {
        // 找到对应的
        if (image) {
          // 去找到具体的文件位置
          Prisma.file
            .findFirst({
              where: {
                id: image.file,
              },
            })
            .then((file) => {
              if (file) {
                const result: ImageItem = {
                  name: image.name,
                  path: file.path,
                  hash: file.hash,
                  type: image.type,
                };
                resolve(packageResponse(response, result));
              } else {
                packageRawErrorResponse(response);
              }
            });
        } else {
          // 没找到, 只能模糊搜索了
        }
      });
    resolve(packageEmptyResponse(response));
  });
}
