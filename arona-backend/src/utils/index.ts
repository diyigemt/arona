import utc from "dayjs/plugin/utc";
import tz from "dayjs/plugin/timezone";
import cpf from "dayjs/plugin/customParseFormat";
import dayjs from "dayjs";
import pinyin from "pinyin";
import { Image } from "@prisma/client";
const TimezoneString = "Asia/Shanghai";
dayjs.extend(utc);
dayjs.extend(tz);
dayjs.extend(cpf);
dayjs.tz.setDefault(TimezoneString);

export const DEFAULT_DAY_FORMAT = "YYYY-MM-DD HH:mm:ss";
export function formatDate2String(
  date: dayjs.Dayjs,
  format: string = DEFAULT_DAY_FORMAT,
) {
  return date.format(format);
}

export function now() {
  return dayjs().tz(TimezoneString);
}

export function nowFormattedString(format: string = DEFAULT_DAY_FORMAT) {
  return now().format(format);
}

export function timezone(
  date?: string | number | dayjs.Dayjs | Date | null | undefined,
  format?: dayjs.OptionType | undefined,
) {
  return dayjs(date, format).tz(TimezoneString);
}

function replaceStopWord(str: string): string {
  return str
    .replace(
      // eslint-disable-next-line max-len
      /[\u3002\uff1f\uff01\uff0c\u3001\uff1b\uff1a\u201c\u201d\u2018\u2019\uff08\uff09\u300a\u300b\u3008\u3009\u3010\u3011\u300e\u300f\u300c\u300d\ufe43\ufe44\u3014\u3015\u2026\u2014\uff5e\ufe4f\uffe5]/g,
      "",
    )
    .replace(/[.,/#!$%^&*;:{}=\-_`~()]/g, "")
    .replace(/\s{2,}/g, " ");
}

export function toPinyin(han: string): string {
  // 去掉标点符号
  const hanR = replaceStopWord(han);
  return pinyin(hanR, { style: "normal" }).flat(1).join("");
}

// 模糊查询实现
const ImageFuzzySearchCache: Image[] = [];

function fuzzySearchImage(name: string) {
  return ImageFuzzySearchCache.filter((it) => it.name === name);
}
