import { Router } from "express";
import ContactRouter from "./contact";
import PingRouter from "./ping";

const router = Router();

router.use("/contacts", ContactRouter);
router.use("/ping", PingRouter);

export default router;
