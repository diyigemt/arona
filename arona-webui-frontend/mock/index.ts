import express from "express";
// @ts-ignore
import cors from "cors";
import bodyParser from "body-parser";
import http from "http";
import router from "./routes";

function main() {
  const app = express();
  const port = 12255;
  app.use(bodyParser.json());
  app.use(cors());
  app.use("/api/v1", router);
  try {
    const httpServer = http.createServer(app);
    httpServer.listen(port, () => {
      console.info(`Http Server running on http://localhost:${port}`);
    });
  } catch (error: any) {
    console.error(`Http Error occurred: ${error.message}`);
  }
}

main();
