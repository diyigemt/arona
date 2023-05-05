import { Command } from "commander";
import { Logger } from "../utils/logger";
import * as process from "process";

const ExitCommand = new Command("exit")
  .description("exit program")
  .option("-s", "second validation")
  .action((options: { s?: boolean }) => {
    if (!options.s) {
      Logger.error("exit need -s for second validation.");
    } else {
      process.exit();
    }
  });

export default ExitCommand;
