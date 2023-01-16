import { Router } from "express";
import { getContactList } from "../controller/contact";
import {ping} from "../controller/ping";

const PingRouter = Router();

PingRouter.get("/", ping);

export default PingRouter;
