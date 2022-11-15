import { forOwn } from "lodash";
import { AronaGachaConfig, AronaGachaConfigMap, AvailableConfig, Config2Form, Config2Map } from "@/interface";
import { fetchAronaConfig, saveAronaConfig } from "@/api/modules/config";
import { errorMessage, successMessage, warningMessage } from "@/utils/message";

interface ILoading {
  loading: boolean;
  confirmLoading: boolean;
}

function buildConfigForm<T extends object>(config: AvailableConfig) {
  const form = reactive<Config2Form<T>>({} as T);
  const loading = reactive<ILoading>({
    loading: false,
    confirmLoading: false,
  });
  function doFetchConfig() {
    loading.loading = true;
    fetchAronaConfig<Config2Map<T>>(config)
      .then((res) => {
        forOwn(res.data, (value, key) => {
          Reflect.set(form, key, value.value);
        });
        loading.loading = false;
      })
      .catch((err) => {
        errorMessage("获取主配置信息失败");
        console.log(err);
        loading.loading = false;
      });
  }
  function doSave() {
    loading.confirmLoading = true;
    saveAronaConfig<T, null>(form, config)
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
