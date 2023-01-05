import { ElMessage, ElMessageBox } from "element-plus";
import {
  ElMessageBoxOptions,
  MessageBoxData,
} from "element-plus/es/components/message-box/src/message-box.type";

export function infoMessage(info: any) {
  return ElMessage.info(info);
}
export function successMessage(info: any) {
  return ElMessage.success(info);
}
export function warningMessage(info: any) {
  return ElMessage.warning(info);
}
export function errorMessage(info: any) {
  return ElMessage.error(info);
}

export function IPrompt(title: string, message: string, config?: ElMessageBoxOptions): Promise<MessageBoxData> {
  return ElMessageBox.prompt(message, title, config);
}

export function IConfirm(title: string, message: string, config?: ElMessageBoxOptions) {
  return ElMessageBox.confirm(message, title, config);
}
