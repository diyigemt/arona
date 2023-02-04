import { Router } from "express";
import { getFileById, uploadImage } from "../controller/file";

const FileRouter = Router();

FileRouter.post("/image", uploadImage);
FileRouter.get("/image", getFileById);

export default FileRouter;
