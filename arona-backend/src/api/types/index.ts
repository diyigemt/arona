export enum HttpCode {
  HTTP_OK = 200,
  BAD_REQUEST = 400,
  INTERNAL_SERVER_ERROR = 500,
}

export interface ServerResponse<T> {
  status: number;
  data: T;
  message: string;
}
