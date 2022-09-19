import yaml
import shutil
import dotmap
from yaml.loader import SafeLoader

def do_transfer(source, type, final):
    dict = dotmap.DotMap({})
    for raw in filter(lambda item: item.type == type, source.override):
        name = raw.name
        value = raw.value
        if len(dict[value]) == 0:
            dict[value] = [name]
        else:
            dict[value].append(name)
    for key in dict:
        final["override"].append({
            "type": type,
            "name": ", ".join(dict[key]),
            "value": key
        })
    return dict

if __name__ == "__main__":
    final = {
        "override": []
    }
    source = {} # 保存源文件
    with open(r"./arona-trainer.yml", "r", encoding="UTF-8") as f:
        source = dotmap.DotMap(yaml.load(f, SafeLoader))
    shutil.copyfile(r"./arona-trainer.yml", r"./arona-trainer_backup.yml")
    do_transfer(source, "RAW", final)
    do_transfer(source, "IMAGE", final)
    do_transfer(source, "CODE", final)
    with open(r"./arona-trainer.yml", "wb") as f:
        f.write(yaml.dump(final, encoding="UTF-8", allow_unicode=True))