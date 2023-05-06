import { Command } from "commander";
import * as process from "process";

const ExitCommand = new Command("exit")
  .exitOverride()
  .description("完全退出程序")
  .requiredOption("-s", "二次验证参数")
  .action(() => {
    process.exit();
  });

export default ExitCommand;
