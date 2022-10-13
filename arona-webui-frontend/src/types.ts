export interface TimeLineProps {
  period?: "millisecond" | "second" | "minute"; // 每格时间间隔
  periodIncrement?: number; // 周期的几倍算一格
  scale?: number; // 放大倍率, 整个组件宽度内展示多少个格子
  total?: number; // 时间总长度
}
