// import { DEBUG } from "../../../index";
import { NextFunction, Request, Response } from "express";
// import { updateOffline, updateOnline } from "../controller/user";
import { getRequestClientVersion, getRequestRemoteIp } from "../utils";
import { Logger } from "../../utils/logger";

const AdminAccessRegexp = /^\/api\/v\d\/admin\/.*/;

function userAccessLogger(): (
  req: Request,
  res: Response,
  next: NextFunction,
) => void {
  return (req, res, next) => {
    // if (DEBUG) {
    //   next();
    //   return;
    // }
    const id = getRequestRemoteIp(req);
    const version = getRequestClientVersion(req);
    const ip = req.header("X-Real-IP"); // 获取nginx提取的form ip
    if (
      (typeof id === "undefined" || id.length < 32 || id.length > 36) &&
      !AdminAccessRegexp.test(req.path)
    ) {
      // 不携带id且不是初次注册请求
      // const path = req.path
      // if (!path || !path.endsWith("/register")) {
      //   res.status(500).send("not user");
      // }
      // 不记录访问状态, 但是打印日志, 记录可疑请求
      Logger.warn(
        `unauthorized access: ${req.method}: ${req.path} with ${JSON.stringify(
          req.method.toLowerCase() === "get" ? req.query : req.body,
        )} by ${ip}`,
      );
      next();
      return;
    } else {
      // // 更新用户在线时间
      // if (id) {
      //   updateOffline(id, version).then(() => {
      //     updateOnline(id).then();
      //   });
      // }
      Logger.info(
        `${req.method}: ${req.path} with ${JSON.stringify(
          req.method.toLowerCase() === "get" ? req.query : req.body,
        )} by ${id} using ${version}`,
      );
      next();
      // TODO check AuthHeader
      // UserModelInterface.findByPk(id).then((user) => {
      //   if (!user) {
      //     res.status(500).send("not user");
      //   } else {
      //     Logger.info(`request: ${req.path} with ${JSON.stringify(req.query)} by ${id}`)
      //     next();
      //   }
      // }).catch(() => {
      //   res.status(500).send("server error");
      // })
    }
  };
}

export default userAccessLogger;
