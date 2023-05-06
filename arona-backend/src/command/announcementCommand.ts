import { Command } from "commander";

const AnnouncementCommand = new Command("announce")
  .exitOverride()
  .description("发布一条公告")
  .argument("content", "公告内容")
  .action((options: { content: string }) => {
    console.log(options);
  });

export default AnnouncementCommand;
