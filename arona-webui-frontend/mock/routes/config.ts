import { Router } from "express";
import { getConfig } from "../controller/config";

const ConfigRouter = Router();

ConfigRouter.get("/", getConfig);

export default ConfigRouter;
