import { Request, Response } from "express";
import { HttpCode, ServerResponse } from "../types";
import { AdminAuthToken, ClientVersionHeader, UUIDAuthHeader } from "../const";
import { Logger } from "../../utils/logger";

export function buildServerResponse<T>(
  data: T,
  message = "",
  code = HttpCode.HTTP_OK,
): ServerResponse<T> {
  return { status: code, data: data || ("" as any), message: message };
}

export function getRequestAdminToken(req: Request) {
  return req.header(AdminAuthToken) as string;
}

export function getRequestClientUUID(req: Request) {
  return req.header(UUIDAuthHeader) as string;
}

export function getRequestClientVersion(req: Request) {
  return req.header(ClientVersionHeader) as string;
}

export function getRequestRemoteIp(req: Request) {
  return req.header("X-Real-IP") as string; // 获取nginx提取的from ip
}

export function packageEmptyResponse(response: Response) {
  return packageResponse(response, null);
}

export function packageErrorResponse(
  response: Response,
  code: HttpCode,
  message = "",
) {
  return packageErrorResponseEx(response, null, code, message);
}

export function packageErrorResponseEx<T>(
  response: Response,
  data: T,
  code: HttpCode,
  message = "",
) {
  return response
    .status(HttpCode.HTTP_OK)
    .send(buildServerResponse(data, message, code));
}

export function packageRawErrorResponse(
  response: Response,
  code = HttpCode.INTERNAL_SERVER_ERROR,
) {
  return response.status(code).send();
}

export function packageResponse<T>(
  response: Response,
  data: T,
  message = "",
  code = HttpCode.HTTP_OK,
) {
  return response
    .status(HttpCode.HTTP_OK)
    .send(buildServerResponse(data, message, code));
}
