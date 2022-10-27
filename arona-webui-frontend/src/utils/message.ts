import { ElMessage } from "element-plus";

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
