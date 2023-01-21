import { Router } from "express";
import ContactRouter from "./contact";
import PingRouter from "./ping";
import BlocklyRouter from "./blockly";

const router = Router();

router.use("/contacts", ContactRouter);
router.use("/ping", PingRouter);
router.use("/blockly", BlocklyRouter);

export default router;
