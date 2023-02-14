import { Router } from "express";
import { getBotList } from "../controller/mirai";

const MiraiRouter = Router();

MiraiRouter.get("/bot", getBotList);

export default MiraiRouter;
