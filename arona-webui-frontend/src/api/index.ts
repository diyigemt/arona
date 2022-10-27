import service from "./http";

export function heartbeat(url: string) {
  return service.raw<string>({
    baseURL: url,
    url: "/ping",
    method: "GET",
  });
}
