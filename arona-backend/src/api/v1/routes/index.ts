import { Router } from "express";
import ImageRouter from "./normal/image";

const V1Router = Router();
V1Router.use("/image", ImageRouter);

export default V1Router;
