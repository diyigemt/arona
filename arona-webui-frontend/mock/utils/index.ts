import { Response } from "express";

export function packageResponse<T>(response: Response, data: T) {
  return response.status(200).send(constructMessage<T>(200, data));
}

export function constructMessage<T>(code: number, data: T, message = "") {
  return { code, data: data || ("" as any), message };
}
