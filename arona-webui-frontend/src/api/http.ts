/* eslint-disable prettier/prettier */
import axios, { AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse } from "axios";
import showCodeMessage from "@/api/code";
import { formatJsonToUrlParams, instanceObject } from "@/utils/format";
import { errorMessage, infoMessage, warningMessage } from "@/utils/message";
import {HTTP_OK} from "@/constant/http";

const BASE_PREFIX = import.meta.env.VITE_API_BASEURL;

// 创建实例
const axiosInstance: AxiosInstance = axios.create({
  // 前缀
  baseURL: BASE_PREFIX,
  // 超时
  timeout: 1000 * 30,
  // 请求头
  headers: {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
  },
});

// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface RequestConfig<D = any> extends AxiosRequestConfig<D> {
  // 当网络请求错误时是否显示错误
  showResponseError?: boolean;
  // 当http 200但是 serverResponse.code !== 200 时是否显示 serverResponse.message
  // 默认开启
  showServerResponseError?: boolean;
  version?: "v1" | "v2";
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface ResponseError<T = any, D = any> extends AxiosError<T, D> {
  config: RequestConfig<D>;
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface Response<T = any, D = any> extends AxiosResponse<T, D> {
  config: RequestConfig<D>;
}

// 请求拦截器
axiosInstance.interceptors.request.use(
  (config: RequestConfig) => {
    if (!config.version) {
      config.version = "v1";
      config.baseURL = `${config.baseURL}/api/${config.version}/`
    }
    config.showServerResponseError = config.showServerResponseError || true;
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

// 响应拦截器
axiosInstance.interceptors.response.use(
  (response: Response) => {
    if (response.status === HTTP_OK) {
      const resp = response.data as ServerResponse<unknown>;
      if (resp && resp.code !== HTTP_OK && resp.message && response.config.showServerResponseError) {
        warningMessage(resp.message);
      }
      return response.data;
    }
    infoMessage(JSON.stringify(response.status));
    return response;
  },
  (error: ResponseError) => {
    const { response, config } = error;
    if (response) {
      if (config.showResponseError) {
        errorMessage(showCodeMessage(response.status));
      }
      return Promise.reject(response.data);
    }
    if (config.showResponseError) {
      warningMessage("网络连接异常,请稍后再试!");
    }
    return Promise.reject(error);
  },
);

const service = {
  get<T>(url: string, data?: object): Promise<ServerResponse<T>> {
    return axiosInstance.get(url, { params: data });
  },

  post<T>(url: string, data?: object): Promise<ServerResponse<T>> {
    return axiosInstance.post(url, data);
  },

  put<T>(url: string, data?: object): Promise<ServerResponse<T>> {
    return axiosInstance.put(url, data);
  },

  delete<T>(url: string, data?: object): Promise<ServerResponse<T>> {
    return axiosInstance.delete(url, data);
  },

  upload(url: string, file: FormData | File) {
    const data = new FormData();
    data.set("file", (file as File).stream());
    axiosInstance({
      url,
      data: data,
      headers: { "Content-Type": "multipart/form-data" },
    }) as unknown as Promise<ServerResponse<string>>
  },

  download: (url: string, data: instanceObject) => {
    window.location.href = `${BASE_PREFIX}/${url}?${formatJsonToUrlParams(data)}`;
  },

  raw<T>(config: RequestConfig): Promise<ServerResponse<T>> {
    return axiosInstance(config) as unknown as Promise<ServerResponse<T>>;
  },
};

export function updateAPIService(host: string, port: number) {
  axiosInstance.defaults.baseURL = currentAPI(host, port);
}

export function currentAPI(host: string, port: number) {
  return `${host}:${port}`;
}

interface ServerResponse<T> {
  code: number;
  message: string;
  data: T;
}

export default service;
