import { Router } from "express";
import { queryImage } from "../../controller/normal/image";

const ImageRouter = Router();
ImageRouter.get("/", queryImage);
export default ImageRouter;
