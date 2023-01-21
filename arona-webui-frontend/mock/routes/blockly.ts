import { Router } from "express";
import { getBlocklyProject } from "../controller/blockly";

const BlocklyRouter = Router();

BlocklyRouter.get("/commit", getBlocklyProject);

export default BlocklyRouter;
