import { Response } from "express";
import { HTTP_OK } from "../../src/constant/http";

export function packageResponseNull(response: Response) {
  return response.status(HTTP_OK).send(constructMessage(HTTP_OK, null));
}

export function packageResponse<T>(response: Response, data: T) {
  return response.status(HTTP_OK).send(constructMessage<T>(HTTP_OK, data));
}

export function constructMessage<T>(code: number, data: T, message = "") {
  return { code, data: data || ("" as any), message };
}
