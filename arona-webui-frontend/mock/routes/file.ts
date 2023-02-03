import { Router } from "express";
import { uploadImage } from "../controller/file";

const FileRouter = Router();

FileRouter.get("/image", uploadImage);

export default FileRouter;
