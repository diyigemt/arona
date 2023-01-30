import { Router } from "express";
import { getReplyGroup, getReplyLabel } from "../controller/reply";

const ReplyRouter = Router();

ReplyRouter.get("/group", getReplyGroup);
ReplyRouter.get("/label", getReplyLabel);

export default ReplyRouter;
