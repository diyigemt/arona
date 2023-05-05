import { PrismaClient } from "@prisma/client";
import { Logger } from "../utils/logger";

const Prisma = new PrismaClient({
  log: [
    {
      emit: "event",
      level: "query",
    },
  ],
});

Prisma.$on("query", (e) => {
  Logger.info(e.query);
  Logger.info(e.params);
  Logger.info(e.timestamp);
});

export default Prisma;
