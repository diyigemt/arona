import { Router } from "express";
import ContactRouter from "./contact";
import PingRouter from "./ping";
import BlocklyRouter from "./blockly";
import ConfigRouter from "./config";
import ReplyRouter from "./reply";

const router = Router();

router.use("/contacts", ContactRouter);
router.use("/ping", PingRouter);
router.use("/blockly", BlocklyRouter);
router.use("/config", ConfigRouter);
router.use("/reply", ReplyRouter);

export default router;
