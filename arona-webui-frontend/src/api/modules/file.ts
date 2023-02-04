import service, { currentAPI } from "@/api/http";

const FileApi = {
  buildFileDownloadPath(id: string) {
    return `${currentAPI()}/api/v1/file/image?id=${id}`;
  },
  fetchFileById(id: string) {
    return service.download(id);
  },
};

export default FileApi;
