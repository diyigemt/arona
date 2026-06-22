"""检查 image/some 中的文件名是否能匹配到学生中文名(cnName)。

数据来源: config/student_cache.json, 其结构为 {日文名: {"cnName": str, ...}}。
some 文件名约定: 主名在前, 可用 "_" 追加别名(主名_别名1_别名2...),
个别文件名可能带 "国服" 区服标记。本脚本以"主名"(第一个 "_" 段, 去掉区服标记)
与 cnName 做精确匹配。匹配前先经 config/name_map_dict.txt 做命名更正(以 cnName 为准),
仍无法匹配的即为真正的差异(通常是 cnName 缓存里尚未收录的学生), 输出供人工核对。
"""

import os
import sys
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
STUDENT_CACHE_PATH = ROOT / "config" / "student_cache.json"
NAME_MAP_PATH = ROOT / "config" / "name_map_dict.txt"
SOME_IMAGE_DIR = ROOT / "image" / "some"

# 区服标记: 仅在主名两端出现时剥离, 避免被当成名字的一部分而误报
REGION_MARKERS = ("国服",)


def load_cn_names(path: Path) -> set[str]:
    """读取 student_cache.json, 返回所有非空 cnName 的集合。"""
    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)
    if not isinstance(data, dict):
        raise ValueError(f"{path} 顶层结构应为 dict")
    cn_names: set[str] = set()
    for value in data.values():
        if not isinstance(value, dict):
            continue
        cn_name = value.get("cnName")
        if isinstance(cn_name, str) and cn_name.strip():
            cn_names.add(cn_name.strip())
    return cn_names


def load_name_map(path: Path) -> dict[str, str]:
    """读取 name_map_dict.txt, 返回 {源名: 权威名(cnName)}。

    文件格式为每行 "源名,目标名"。容错: 跳过空行、无逗号行, 以及源名/目标名为空的行;
    只按第一个逗号分割, 保留右侧其余内容。文件不存在时返回空映射(视为无更正)。
    """
    name_map: dict[str, str] = {}
    if not path.is_file():
        return name_map
    with path.open("r", encoding="utf-8") as f:
        for raw_line in f:
            line = raw_line.strip()
            if not line or "," not in line:
                continue
            source, target = line.split(",", maxsplit=1)
            source, target = source.strip(), target.strip()
            if source and target:
                name_map[source] = target
    return name_map


def strip_region_marker(name: str) -> str:
    """剥离主名两端的区服标记(如 国服)。"""
    result = name.strip()
    for marker in REGION_MARKERS:
        if result == marker:
            return ""
        if result.startswith(marker):
            result = result[len(marker):].strip()
        if result.endswith(marker):
            result = result[:-len(marker)].strip()
    return result


def get_primary_name(file_stem: str) -> str:
    """从去扩展名的文件名中取出用于匹配的主名(第一个 "_" 段, 去区服标记)。"""
    parts = [part.strip() for part in file_stem.split("_") if part.strip()]
    # 跳过形如 "国服_某某" 中独立成段的区服标记
    while parts and parts[0] in REGION_MARKERS:
        parts.pop(0)
    if not parts:
        return ""
    return strip_region_marker(parts[0])


def find_unmatched(
    cn_names: set[str],
    name_map: dict[str, str],
    image_dir: Path,
) -> tuple[list[tuple[str, str, str]], list[tuple[str, str]]]:
    """遍历 some 目录, 把主名经映射更正后与 cnName 比对。

    返回 (unmatched, corrected):
    - unmatched: 仍无法匹配的文件, 元素为 (文件名, 原始主名, 更正后主名)。
    - corrected: 经映射更正后匹配上的, 元素为 (原始主名, 更正后主名), 已去重。
    """
    if not image_dir.is_dir():
        raise FileNotFoundError(f"图片目录不存在或不是目录: {image_dir}")
    unmatched: list[tuple[str, str, str]] = []
    corrected: dict[str, str] = {}
    for path in sorted(image_dir.iterdir(), key=lambda p: p.name):
        if not path.is_file() or path.suffix.lower() != ".png":
            continue
        primary = get_primary_name(path.stem)
        fixed = name_map.get(primary, primary)
        if fixed and fixed in cn_names:
            if fixed != primary:
                corrected[primary] = fixed
            continue
        unmatched.append((path.name, primary, fixed))
    return unmatched, sorted(corrected.items())


def main() -> None:
    # Windows 控制台默认非 UTF-8, 中文名会乱码, 这里把输出统一切到 UTF-8
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8")

    cn_names = load_cn_names(STUDENT_CACHE_PATH)
    name_map = load_name_map(NAME_MAP_PATH)
    unmatched, corrected = find_unmatched(cn_names, name_map, SOME_IMAGE_DIR)

    print(f"cnName 总数: {len(cn_names)}")
    print(f"映射条目数: {len(name_map)}")
    print(f"经映射更正后匹配的主名数: {len(corrected)}")
    print(f"仍无法匹配的文件数: {len(unmatched)}")
    if corrected:
        print("经映射更正:")
        for source, target in corrected:
            # 重命名文件
            os.rename(SOME_IMAGE_DIR / (source + ".png"), SOME_IMAGE_DIR / (target + ".png"))
            print(f"- {source} -> {target}")
    if not unmatched:
        return
    print("仍无法匹配的文件:")
    for file_name, primary, fixed in unmatched:
        if fixed != primary:
            print(f"- {file_name}\t(主名: {primary}, 更正后: {fixed})")
        else:
            print(f"- {file_name}\t(主名: {primary})")


if __name__ == "__main__":
    main()
