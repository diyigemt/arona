import { AvailableConfig, Config2Form, Config2Map } from "@/interface";
import { fetchAronaConfig, saveAronaConfig } from "@/api/modules/config";
import { errorMessage, successMessage, warningMessage } from "@/utils/message";
import { fillConfigMap } from "@/utils";

interface ILoading {
  loading: boolean;
  confirmLoading: boolean;
}

// eslint-disable-next-line import/prefer-default-export
export function buildConfigForm<T>(config: AvailableConfig) {
  const form = ref<Config2Form<T>>({} as Config2Form<T>);
  const loading = reactive<ILoading>({
    loading: false,
    confirmLoading: false,
  });
  function doFetchConfig() {
    loading.loading = true;
    fetchAronaConfig<Config2Map<T>>(config)
      .then((res) => {
        fillConfigMap(res.data, form.value);
        loading.loading = false;
      })
      .catch((err) => {
        errorMessage("获取配置信息失败");
        console.log(err);
        loading.loading = false;
      });
  }
  function doSave() {
    loading.confirmLoading = true;
    saveAronaConfig<T, null>(form.value, config)
      .then(() => {
        successMessage("更新成功");
        loading.confirmLoading = false;
      })
      .catch(() => {
        warningMessage("更新失败");
        loading.confirmLoading = false;
      });
  }
  return {
    form,
    loading,
    doFetchConfig,
    doSave,
  };
}
