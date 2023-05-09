import { NextFunction, Request, Response } from "express";
import * as crypto from "crypto";
import { Logger } from "../../utils/logger";
import {
  getRequestAdminToken,
  getRequestRemoteIp,
  packageRawErrorResponse,
} from "../utils";
const adminTokenEncrypt = process.env.ADMIN_SECRET;

function adminAccessInterceptor(): (
  req: Request,
  res: Response,
  next: NextFunction,
) => void {
  return (req, res, next) => {
    const ip = getRequestRemoteIp(req);
    const token = getRequestAdminToken(req);
    if (typeof token !== "string") {
      Logger.warn(
        `unauthorized access: ${req.method}: ${req.path} with ${JSON.stringify(
          req.method.toLowerCase() === "get" ? req.query : req.body,
        )} by ${ip}`,
      );
      Logger.warn(
        `failed authorized access: ${req.method}: ${
          req.path
        } with ${JSON.stringify(
          req.method.toLowerCase() === "get" ? req.query : req.body,
        )} by ${ip}`,
      );
      packageRawErrorResponse(res);
      return;
    }
    const encrypt = crypto.createHash("md5").update(token).digest("hex");
    if (encrypt !== adminTokenEncrypt) {
      packageRawErrorResponse(res);
      return;
    }
    Logger.info(
      `admin access: ${req.method}: ${req.path} with ${JSON.stringify(
        req.method.toLowerCase() === "get" ? req.query : req.body,
      )} by ${ip}`,
    );
    next();
  };
}

export default adminAccessInterceptor;
