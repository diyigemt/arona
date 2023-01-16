import { Router } from "express";
import { getContactList } from "../controller/contact";

const ContactRouter = Router();

ContactRouter.get("/", getContactList);

export default ContactRouter;
