import service from "@/api/http";
import {currentAPI} from "@/api/adapter/localhost";

const FileApi = {
  buildFileDownloadPath(id: string) {
    return `${currentAPI()}/api/v1/file/image?id=${id}`;
  },
  fetchFileById(id: string) {
    return service.download(id);
  },
};

export default FileApi;
