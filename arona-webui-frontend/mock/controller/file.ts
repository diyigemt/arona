import { Request, Response } from "express";
import { UploadedFile } from "express-fileupload";
import { packageResponse, packageResponseError } from "../utils";

// eslint-disable-next-line import/prefer-default-export
export function uploadImage(request: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    if (request.files && request.files.file) {
      const file = request.files.file as UploadedFile;
      const fileName = request.header("arona-file-name")
        ? decodeURI(request.header("arona-file-name") as string)
        : file.name;
      file.mv(`${__dirname}/files/${fileName}`).then(() => {
        resolve(packageResponse(response, "1000002"));
      });
    } else {
      resolve(packageResponseError(response, "没有文件"));
    }
  });
}

const fileDict: Record<string, string> = {
  "1000001": "rua.png",
  "1000002": "neko.png",
};

export function getFileById(request: Request, response: Response) {
  const id = request.query.id as string;
  response.sendFile(`${__dirname}/files/${fileDict[id]}`);
}
