import { successMessage } from "@/utils/message";
import service from "./http";
import emitter from "@/utils/config/emitter";

// eslint-disable-next-line import/prefer-default-export
export function heartbeat() {
  return new Promise((resolve) => {
    service
      .raw<string>({
        url: "/ping",
        method: "GET",
        showResponseError: false,
      })
      .then((res) => {
        if (res.data === "pong") {
          successMessage("连接成功");
          emitter.emit("api-update");
          resolve(true);
        }
        resolve(false);
      })
      .catch(() => {
        resolve(false);
      });
  });
}
