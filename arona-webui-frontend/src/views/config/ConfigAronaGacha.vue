<template>
  <el-form v-loading="loading.loading" :model="form" label-width="180px" label-position="left">
    <el-form-item label="star1Rate">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.star1Rate" />
        </el-col>
        <el-col :span="12">
          <div>1星总出率百分比</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="star2Rate">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.star2Rate" />
        </el-col>
        <el-col :span="12">
          <div>2星总出率百分比</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="star3Rate">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.star3Rate" />
        </el-col>
        <el-col :span="12">
          <div>3星总出率百分比</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="star2PickupRate">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.star2PickupRate" />
        </el-col>
        <el-col :span="12">
          <div>2星限定出率百分比</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="star3PickupRate">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.star3PickupRate" />
        </el-col>
        <el-col :span="12">
          <div>3星限定出率百分比</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="activePool">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-select v-model="form.activePool" class="w-full">
            <el-option v-for="(e, index) in select.pools" :key="index" :value="e.id" :label="e.name + '(' + e.id + ')'">
              {{ e.name }}({{ e.id }})
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="12">
          <div>当前激活的池子</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="revokeTime">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.revokeTime" />
        </el-col>
        <el-col :span="12">
          <div>撤回结果信息防止刷屏 撤回时间间隔(单位为秒) 为0表示不撤回</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="limit">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.limit" />
        </el-col>
        <el-col :span="12">
          <div>每日限制次数, 0表示不限制</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="day">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.day" disabled />
        </el-col>
        <el-col :span="12">
          <div>上次限制更新时间, 自动维护</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading.confirmLoading" @click="doSave">保存</el-button>
      <el-button @click="doFetchConfig">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { AronaGachaConfig, AvailableConfig } from "@/interface";
import { warningMessage } from "@/utils/message";
import { GachaPool } from "@/interface/modules/gacha";
import { queryGachaPool } from "@/api/modules/db";
import { buildConfigForm } from "@/views/config/util";

const { form, loading, doFetchConfig, doSave } = buildConfigForm<AronaGachaConfig>(AvailableConfig.AronaGachaConfig);

const select = reactive<Select>({
  pools: [],
});

onMounted(() => {
  queryGachaPool()
    .then((res) => {
      select.pools = res.data;
    })
    .catch((err) => {
      warningMessage("获取卡池列表失败");
      console.log(err);
    });
  doFetchConfig();
});

interface Select {
  pools: GachaPool[];
}
</script>

<style lang="scss" scoped></style>
