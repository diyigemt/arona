import { Logger } from "./utils/logger";
import http from "http";
import fs from "fs";
import https from "https";
import express from "express";
import * as nodejieba from "nodejieba";
// @ts-ignore
import bodyParser from "body-parser";
import readline from "readline";
import CommandParser from "./command";
import Prisma from "./db";

const reader = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
});
reader.on("line", (input) => {
  try {
    CommandParser.parse(input.split(" "), { from: "user" });
    // eslint-disable-next-line no-empty
  } catch (e: unknown) {}
});

async function main() {
  const app = express();
  const httpPort = 12201;
  const httpsPort = 12200;
  app.use(bodyParser.json());
  await Prisma.$connect();
  // 初始化结巴分词
  nodejieba.load({
    userDict: "src/dict/student",
  });
  try {
    const httpServer = http.createServer(app);
    httpServer.listen(httpPort, () => {
      Logger.info(`Http Server running on http://localhost:${httpPort}`);
    });
  } catch (error: any) {
    Logger.error(`Http Error occurred: ${error.message}`);
  }

  try {
    // const privateKey = fs.readFileSync(
    //   "/etc/letsencrypt/live/calibre.diyigemt.net/privkey.pem",
    //   "utf-8"
    // );
    // const certificate = fs.readFileSync(
    //   "/etc/letsencrypt/live/calibre.diyigemt.net/fullchain.pem",
    //   "utf-8"
    // );
    const privateKey = fs.readFileSync(
      "/etc/letsencrypt/live/arona.diyigemt.com/privkey.pem",
      "utf-8",
    );
    const certificate = fs.readFileSync(
      "/etc/letsencrypt/live/arona.diyigemt.com/fullchain.pem",
      "utf-8",
    );
    const credentials = { key: privateKey, cert: certificate };
    const httpsServer = https.createServer(credentials, app);
    httpsServer.listen(httpsPort, () => {
      Logger.info(`Https Server running on http://localhost:${httpsPort}`);
    });
  } catch (error: any) {
    Logger.error(`Https Error occurred: ${error.message}`);
  }
}

main().then();

function globalErrorHandler(reason: any, p?: Promise<unknown>) {
  Logger.error("Unhandled: ", reason, p);
}
process.on("unhandledRejection", globalErrorHandler);
