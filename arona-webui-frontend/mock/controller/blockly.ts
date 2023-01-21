import { Request, Response } from "express";
import { packageResponse } from "../utils";
import { BlocklyProject } from "../../src/interface/modules/blockly";
// eslint-disable-next-line import/prefer-default-export
export function getBlocklyProject(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const data: BlocklyProject[] = [];
      resolve(packageResponse(response, data));
    }, 50);
  });
}
