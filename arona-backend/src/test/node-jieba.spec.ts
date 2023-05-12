import * as nodejieba from "nodejieba";
import PinyinMatch from "pinyin-match";
import { toPinyin } from "../utils";

const cut = nodejieba.cut("白亚的预告函~虚饰之馆与美学的所在~");
console.log(cut);
const match = PinyinMatch.match("黑色的", toPinyin(cut[0]));
test("", () => {
  expect(match).toBe(true);
});
