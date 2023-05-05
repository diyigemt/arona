import log4js from "log4js";

log4js.configure({
  appenders: {
    file: {
      type: "dateFile",
      filename: "./log/log",
      keepFileExt: true,
      numBackups: 365,
    },
    console: {
      type: "console",
    },
  },
  categories: {
    default: {
      appenders: ["file", "console"],
      level: "debug",
    },
    database: {
      appenders: ["file"],
      level: "debug",
    },
  },
});

export const Logger = log4js.getLogger();
export const DatabaseLogger = log4js.getLogger("database");