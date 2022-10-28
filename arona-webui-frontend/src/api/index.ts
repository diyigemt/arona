import { successMessage } from "@/utils/message";
import service from "./http";

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
          resolve(true);
        }
        resolve(false);
      })
      .catch(() => {
        resolve(false);
      });
  });
}
