<template>
  <el-collapse v-model="active">
    <el-collapse-item v-for="(e, index) in testData.rules" :key="index" :name="index" :title="e.name">
      <el-table :data="[e.rule]" row-key="id" border default-expand-all>
        <el-table-column prop="level" label="层级">
          <template #default="{ row }">
            <div :style="{ '--level': row.level }" class="table-cell">
              <div style="margin-bottom: 16px">第{{ row.level }}层</div>
              <el-form :model="row">
                <el-form-item label="规则类型">
                  <el-select v-model="row.leaf" @change="switchNodeType(row)">
                    <el-option label="判断规则" :value="true" />
                    <el-option label="条件规则" :value="false" />
                  </el-select>
                </el-form-item>
                <div v-if="row.leaf">
                  <el-form-item label="判断">
                    <el-select v-model="row.type">
                      <el-option
                        v-for="(j, jIndex) in Object.keys(AronaReplyConfigMatchType)"
                        :key="index + '' + jIndex"
                        :label="AronaReplyConfigMapper[j]"
                        :value="j"
                      />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="值">
                    <el-input v-model="row.value" />
                  </el-form-item>
                </div>
                <div v-if="!row.leaf">
                  <el-form-item label="条件">
                    <el-select v-model="row.condition">
                      <el-option
                        v-for="(j, jIndex) in Object.keys(AronaReplyConfigCondition)"
                        :key="index + '' + jIndex"
                        :label="AronaReplyConfigMapper[j]"
                        :value="j"
                      />
                    </el-select>
                  </el-form-item>
                </div>
              </el-form>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-collapse-item>
  </el-collapse>
  <el-row style="margin-top: 16px">
    <el-button type="primary" :loading="loading.confirmLoading" @click="doSave">保存</el-button>
    <el-button @click="doFetchConfig">重置</el-button>
  </el-row>
</template>

<script setup lang="ts">
import {
  AronaReplyConfig,
  AronaReplyConfigCondition,
  AronaReplyConfigMatchType,
  AronaReplyConfigReplyMatchTree,
  AvailableConfig,
} from "@/interface";
import { buildConfigForm } from "@/views/config/util";
import { AronaReplyConfigMapper } from "@/constant";
import { Group } from "@/types/contact";
import { fetchBotContacts } from "@/api/modules/contact";
import { warningMessage } from "@/utils/message";
import { deepCopy } from "@/utils";

const active = ref<string[]>();
const { form, loading, doFetchConfig, doSave } = buildConfigForm<AronaReplyConfig>(AvailableConfig.AronaReplyConfig);
const select = reactive<Select>({
  groups: [],
});
// @ts-ignore
const testData = ref<AronaReplyConfig>({
  rules: [
    {
      name: "测试测试",
      rule: {
        left: {
          // @ts-ignore
          type: "SUFFIX",
          value: "1231312",
          leaf: false,
        },
        right: {
          // @ts-ignore
          type: "SUFFIX",
          value: "1231312",
          leaf: false,
        },
        // @ts-ignore
        condition: "AND",
        leaf: true,
      },
      messages: [
        {
          message: "123",
          weight: 1,
        },
      ],
      // @ts-ignore
      messageType: "MESSAGE",
    },
  ],
});
function maps(rule: AronaReplyConfigReplyMatchTree, path: string, level = 0) {
  if (rule.left) {
    rule.children = [maps(rule.left, `${path}-left`, level + 1)];
  }
  if (rule.right) {
    rule.children?.push(maps(rule.right, `${path}-right`, level + 1));
  }
  rule.id = path;
  rule.level = level;
  rule.leaf = !rule.left;
  return rule;
}
function translate(rule: AronaReplyConfig) {
  rule.rules = rule.rules.map((item, index) => {
    item.rule = maps(item.rule, "root");
    item.rule.id = `root-${index}`;
    return item;
  });
}
function switchNodeType(node: AronaReplyConfigReplyMatchTree) {
  if (node.leaf) {
    // 如果是判断规则, 直接保存选择分支(不管有没有)
    node.childrenCache = deepCopy(node.children);
    node.children = [];
  } else {
    // 是否是从判断规则切换过来的
    // 有cache, 说明是条件->判断->条件
    // eslint-disable-next-line no-lonely-if
    if (node.childrenCache) {
      node.children = deepCopy(node.childrenCache);
    } else {
      // 没有cache, 说明是直接判断->条件, 新建两个孩子
      const left: AronaReplyConfigReplyMatchTree = {
        id: `${node.id}-left`,
        level: node.level + 1,
        leaf: true,
      };
      const right: AronaReplyConfigReplyMatchTree = {
        id: `${node.id}-right`,
        level: node.level + 1,
        leaf: true,
      };
      node.children = [left, right];
    }
  }
}

onMounted(() => {
  translate(testData.value as any);
  fetchBotContacts()
    .then((res) => {
      select.groups = res.data.groups;
    })
    .catch((err) => {
      warningMessage("获取bot群列表失败");
      console.log(err);
    });
  // doFetchConfig();
});
interface Select {
  groups: Omit<Group, "permission">[];
}
</script>

<style lang="scss" scoped>
.table-cell {
  padding-left: calc(16px * var(--level));
}
</style>
