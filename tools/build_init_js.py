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

STUDENTS_URL = "https://schaledb.com/data/jp/students.min.json"
ITEMS_URL = "https://schaledb.com/data/zh/items.min.json"
EQUIPMENT_URL = "https://schaledb.com/data/zh/equipment.min.json"

HTTP_TIMEOUT = 20
RETRY_BACKOFF_SECONDS = (1, 2)
USER_AGENT = "arona-tools/1.0"

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

ITEM_BOOK_SUBCATEGORIES = {"BookItem", "CDItem"}

BASE_INDENT = "  "


def fetch_json(url: str) -> dict[str, Any]:
    request = urllib.request.Request(
        url,
        headers={
            "User-Agent": USER_AGENT,
            "Accept": "application/json",
            "Accept-Encoding": "identity",
        },
    )
    last_error: Exception | None = None
    for attempt in range(len(RETRY_BACKOFF_SECONDS) + 1):
        try:
            with urllib.request.urlopen(request, timeout=HTTP_TIMEOUT) as response:
                payload = response.read()
            print(f"[build_init_js] fetched {url} bytes={len(payload)}")
            return json.loads(payload.decode("utf-8"))
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
        student["Name"] = record["Name"]
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


def build_items(source: dict[str, Any]) -> list[dict[str, Any]]:
    items: list[dict[str, Any]] = []
    for record in sort_by_id(source):
        if (
            record.get("Category") == "Material"
            and record.get("SubCategory") in ITEM_BOOK_SUBCATEGORIES
        ):
            items.append(rename_id(record))
    return items


def build_equipments(source: dict[str, Any]) -> list[dict[str, Any]]:
    return [rename_id(record) for record in sort_by_id(source)]


def main() -> None:
    init_text = INIT_JS.read_text(encoding="utf-8")
    student_template = extract_template_object(init_text, "students")

    students = build_students(fetch_json(STUDENTS_URL), student_template)
    items = build_items(fetch_json(ITEMS_URL))
    equipments = build_equipments(fetch_json(EQUIPMENT_URL))

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


if __name__ == "__main__":
    main()
