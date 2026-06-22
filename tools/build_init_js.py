from __future__ import annotations

import copy
import http.client
import json
import re
import socket
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any

ROOT = Path(__file__).resolve().parent
PLAYWRIGHT_DIR = ROOT / "playwright"
INIT_JS = PLAYWRIGHT_DIR / "init.js"
ICON_DIR = PLAYWRIGHT_DIR / "im"
ROUTE_OVERRIDES_JSON = PLAYWRIGHT_DIR / "route_overrides.json"

STUDENTS_URL = "https://schaledb.com/data/jp/students.min.json"
ITEMS_URL = "https://schaledb.com/data/zh/items.min.json"
EQUIPMENT_URL = "https://schaledb.com/data/zh/equipment.min.json"
EQUIPMENT_ICON_URL_TEMPLATE = "https://schaledb.com/images/equipment/icon/{icon}.webp"
ITEM_ICON_URL_TEMPLATE = "https://schaledb.com/images/item/icon/{icon}.webp"
GAME_DB_ROUTE_URL_TEMPLATE = "https://ba.game-db.tw/images/items/{icon}.png"
ROUTE_LOCAL_PATH_TEMPLATE = "playwright/im/{icon}.webp"

HTTP_TIMEOUT = 20
RETRY_BACKOFF_SECONDS = (1, 2)
USER_AGENT = "arona-tools/1.0"
EQUIPMENT_ICON_TIER = 9

EQUIPMENT_TYPE_MAP = {
    "Hat": 1,
    "Gloves": 2,
    "Shoes": 3,
    "Bag": 4,
    "Badge": 5,
    "Hairpin": 6,
    "Charm": 7,
    "Watch": 8,
    "Necklace": 9,
}

ITEM_BOOK_SUBCATEGORIES = {"BookItem", "CDItem", "Artifact"}

BASE_INDENT = "  "


def fetch_bytes(url: str, accept: str) -> bytes:
    request = urllib.request.Request(
        url,
        headers={
            "User-Agent": USER_AGENT,
            "Accept": accept,
            "Accept-Encoding": "identity",
        },
    )
    last_error: Exception | None = None
    for attempt in range(len(RETRY_BACKOFF_SECONDS) + 1):
        try:
            with urllib.request.urlopen(request, timeout=HTTP_TIMEOUT) as response:
                payload = response.read()
            print(f"[build_init_js] fetched {url} bytes={len(payload)}")
            return payload
        except (
            urllib.error.URLError,
            http.client.IncompleteRead,
            ConnectionError,
            socket.timeout,
            TimeoutError,
        ) as error:
            last_error = error
            if attempt < len(RETRY_BACKOFF_SECONDS):
                time.sleep(RETRY_BACKOFF_SECONDS[attempt])
    raise RuntimeError(f"failed to fetch {url}: {last_error}") from last_error


def fetch_json(url: str) -> dict[str, Any]:
    payload = fetch_bytes(url, "application/json")
    return json.loads(payload.decode("utf-8"))


def sort_by_id(records: dict[str, Any]) -> list[dict[str, Any]]:
    return sorted(records.values(), key=lambda record: record["Id"])


def find_matching_bracket(text: str, start: int, open_ch: str, close_ch: str) -> int:
    depth = 0
    in_string = False
    escape = False
    for index in range(start, len(text)):
        ch = text[index]
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
        elif ch == open_ch:
            depth += 1
        elif ch == close_ch:
            depth -= 1
            if depth == 0:
                return index
    raise ValueError(f"no matching {close_ch!r} for {open_ch!r} at offset {start}")


def find_array_span(text: str, name: str) -> tuple[int, int]:
    match = re.search(rf"\bwindow\.{re.escape(name)}\s*=\s*\[", text)
    if match is None:
        raise ValueError(f"window.{name} assignment not found in init.js")
    array_start = match.end() - 1
    array_end = find_matching_bracket(text, array_start, "[", "]")
    return array_start, array_end + 1


def extract_template_object(text: str, name: str) -> dict[str, Any]:
    array_start, array_end = find_array_span(text, name)
    object_start = text.find("{", array_start, array_end)
    if object_start == -1:
        raise ValueError(f"window.{name} has no template object to derive from")
    object_end = find_matching_bracket(text, object_start, "{", "}")
    return json.loads(text[object_start : object_end + 1])


def format_js_array(items: list[dict[str, Any]]) -> str:
    dumped = json.dumps(items, ensure_ascii=False, indent=2)
    return dumped.replace("\n", "\n" + BASE_INDENT)


def replace_array(text: str, name: str, items: list[dict[str, Any]]) -> str:
    start, end = find_array_span(text, name)
    return text[:start] + format_js_array(items) + text[end:]


def convert_equipment(values: list[str]) -> list[int]:
    return [EQUIPMENT_TYPE_MAP[value] for value in values]


def build_students(
    source: dict[str, Any],
    template: dict[str, Any],
) -> list[dict[str, Any]]:
    students: list[dict[str, Any]] = []
    for record in sort_by_id(source):
        student = copy.deepcopy(template)
        student["ID"] = record["Id"]
        student["Name"] = record["Name"].replace("（", "(").replace("）", ")").strip()
        student["NameEN"] = ""
        student["NameTW"] = ""
        student["DevName"] = record["DevName"]
        student["Icon"] = ""
        student["Star"] = record["StarGrade"]
        student["SkillEx"] = record["SkillExMaterial"]
        student["SkillExAmount"] = record["SkillExMaterialAmount"]
        student["Skill"] = record["SkillMaterial"]
        student["SkillAmount"] = record["SkillMaterialAmount"]
        student["Equipment"] = convert_equipment(record["Equipment"])
        students.append(student)
    return students


def rename_id(record: dict[str, Any]) -> dict[str, Any]:
    renamed: dict[str, Any] = {"ID": record["Id"]}
    for key, value in record.items():
        if key != "Id":
            renamed[key] = value
    return renamed


def is_material_book_item(record: dict[str, Any]) -> bool:
    return (
        record.get("Category") == "Material"
        and record.get("SubCategory") in ITEM_BOOK_SUBCATEGORIES
    )


def build_items(source: dict[str, Any]) -> list[dict[str, Any]]:
    return [rename_id(record) for record in sort_by_id(source) if is_material_book_item(record)]


def build_equipments(source: dict[str, Any]) -> list[dict[str, Any]]:
    return [rename_id(record) for record in sort_by_id(source)]


def collect_equipment_icons(source: dict[str, Any]) -> set[str]:
    return {
        record["Icon"]
        for record in source.values()
        if record.get("Tier") == EQUIPMENT_ICON_TIER and record.get("Icon")
    }


def collect_item_icons(source: dict[str, Any]) -> set[str]:
    return {
        record["Icon"]
        for record in source.values()
        if is_material_book_item(record) and record.get("Icon")
    }


ICON_STATUS_EXISTED = "existed"
ICON_STATUS_DOWNLOADED = "downloaded"
ICON_STATUS_FAILED = "failed"


def download_icon(icon: str, url_template: str) -> str:
    dest_path = ICON_DIR / f"{icon}.webp"
    if dest_path.exists():
        print(f"[build_init_js] skip icon (exists): {icon}")
        return ICON_STATUS_EXISTED
    try:
        payload = fetch_bytes(url_template.format(icon=icon), "image/webp,image/*")
        dest_path.write_bytes(payload)
        print(f"[build_init_js] wrote icon {dest_path.name}")
        return ICON_STATUS_DOWNLOADED
    except Exception as error:
        print(f"[build_init_js] failed icon {icon}: {error}")
        return ICON_STATUS_FAILED


def download_icons_batch(icons: set[str], url_template: str) -> tuple[set[str], int, int, int]:
    ICON_DIR.mkdir(parents=True, exist_ok=True)
    successful: set[str] = set()
    existed = 0
    downloaded = 0
    failed = 0
    for icon in sorted(icons):
        status = download_icon(icon, url_template)
        if status == ICON_STATUS_EXISTED:
            successful.add(icon)
            existed += 1
        elif status == ICON_STATUS_DOWNLOADED:
            successful.add(icon)
            downloaded += 1
        else:
            failed += 1
    return successful, existed, downloaded, failed


def write_route_overrides(icons: set[str]) -> None:
    overrides = {
        GAME_DB_ROUTE_URL_TEMPLATE.format(icon=icon): ROUTE_LOCAL_PATH_TEMPLATE.format(icon=icon)
        for icon in sorted(icons)
    }
    ROUTE_OVERRIDES_JSON.write_text(
        json.dumps(overrides, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def main() -> None:
    init_text = INIT_JS.read_text(encoding="utf-8")
    student_template = extract_template_object(init_text, "students")

    students_source = fetch_json(STUDENTS_URL)
    items_source = fetch_json(ITEMS_URL)
    equipments_source = fetch_json(EQUIPMENT_URL)

    students = build_students(students_source, student_template)
    items = build_items(items_source)
    equipments = build_equipments(equipments_source)

    print(
        f"[build_init_js] students={len(students)} "
        f"items={len(items)} equipments={len(equipments)}"
    )

    new_text = init_text
    new_text = replace_array(new_text, "students", students)
    new_text = replace_array(new_text, "items", items)
    new_text = replace_array(new_text, "equipments", equipments)

    INIT_JS.write_text(new_text, encoding="utf-8")
    print(f"[build_init_js] wrote {INIT_JS}")

    equipment_icons = collect_equipment_icons(equipments_source)
    item_icons = collect_item_icons(items_source)
    eq_success, eq_existed, eq_downloaded, eq_failed = download_icons_batch(
        equipment_icons, EQUIPMENT_ICON_URL_TEMPLATE
    )
    it_success, it_existed, it_downloaded, it_failed = download_icons_batch(
        item_icons, ITEM_ICON_URL_TEMPLATE
    )
    successful_icons = eq_success | it_success
    write_route_overrides(successful_icons)

    print(
        f"[build_init_js] icons={len(successful_icons)} "
        f"downloaded={eq_downloaded + it_downloaded} "
        f"existed={eq_existed + it_existed} "
        f"failed={eq_failed + it_failed}"
    )
    print(f"[build_init_js] wrote route overrides {ROUTE_OVERRIDES_JSON}")


if __name__ == "__main__":
    main()
