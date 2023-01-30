export type ReplyItemType = "String" | "Image";
export interface ReplyItem {
  type: ReplyItemType;
  content: string;
}

/**
 * 一个回复的最小单位
 */
export interface ReplyGroup {
  id: number; // 数据库主键
  content: ReplyItem[]; // 内容, 可以是单个句子, 也可以是句子与图片组合, 目前只支持句子
  weight: number; // 内容权重, 内容最终权重 = 句子权重 + 标签权重
  label: number[]; // 所属标签
}

/**
 * 标签定义
 */
export interface ReplyLabel {
  id: number; // 数据库主键
  value: string; // 标签内容
  weight: number; // 标签权重, 内容最终权重 = 句子权重 + 标签权重
}
