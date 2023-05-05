// @ts-ignore
import { Command } from "commander";
import ExitCommand from "./exitCommand";

const CommandParser = new Command();
// eslint-disable-next-line @typescript-eslint/no-empty-function
CommandParser.version("1.0.0").exitOverride();
CommandParser.addCommand(ExitCommand);

export default CommandParser;
