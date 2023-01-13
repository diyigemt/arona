import Blockly, { Block } from "blockly";
import { fetchBotContacts } from "@/api/modules/contact";
import { warningMessage } from "@/utils/message";
import service from "@/api/http";
import { Friend } from "@/types/contact";

export default class BlocklyUtil {
  static groupMemberPool = new Map<string, [string, string][]>();

  static disableBlock(block: Block, warnMessage: string) {
    block.setEnabled(false);
    block.setColour(0);
    block.setWarningText(warnMessage);
  }

  static enableBlock(block: Block) {
    block.setEnabled(true);
    block.setColour(230);
    block.setWarningText(null);
  }

  // eslint-disable-next-line @typescript-eslint/ban-types
  static registerExtensions(name: string, extension: Function) {
    if (Blockly.Extensions.isRegistered(name)) {
      Blockly.Extensions.unregister(name);
    }
    Blockly.Extensions.register(name, extension);
  }

  static registerMixin(
    name: string,
    mixin: unknown,
    helperFn?: () => unknown | undefined,
    blockList?: string[] | undefined,
  ) {
    if (Blockly.Extensions.isRegistered(name)) {
      Blockly.Extensions.unregister(name);
    }
    Blockly.Extensions.registerMutator(name, mixin, helperFn, blockList);
  }

  static findContext(block: Block, type: string) {
    let parent = block.getParent();
    while (parent != null) {
      if (parent.type === type) {
        return parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  static findGroupMember(id: number) {
    // eslint-disable-next-line no-restricted-syntax
    for (const group of this.groupMemberPool) {
      // eslint-disable-next-line no-restricted-syntax
      for (const member of group) {
        if (member === id.toString()) return true;
      }
    }
    return false;
  }

  static wipeDuplicateData<T>(list: T[]): T[] {
    list.sort();
    const res = [list[0]];
    list.forEach((item) => {
      if (item !== res[res.length - 1]) {
        res.push(item);
      }
    });

    return res;
  }
}

// eslint-disable-next-line import/no-mutable-exports
export let groups: [string, string][] = [];
// eslint-disable-next-line import/no-mutable-exports
export let friends: [string, string][] = [];

let groupList: [string, string][] = [];

export function doFetchContacts() {
  groups = [];
  friends = [];
  fetchBotContacts()
    .then((res) => {
      res.data.groups.forEach((item) => {
        groups.push([`${item.name} (${item.id.toString()})`, item.id.toString()]);
      });
      res.data.friends.forEach((item) => {
        friends.push([`${item.name} (${item.id.toString()})`, item.id.toString()]);
      });
    })
    .catch((err) => {
      warningMessage("获取bot联系人列表失败");
      console.log(err);
    });
}

export function doFetchGroupMember(id: number) {
  groupList = [];
  service
    .raw<Friend[]>({
      url: `/contacts/${id}`,
      method: "POST",
    })
    .then((r) => {
      r.data.forEach((item) => {
        let flag = true;
        groupList.forEach((member) => {
          if (member[1] === item.id.toString()) {
            flag = false;
          }
        });
        if (flag) groupList.push([`${item.remark} (${item.id})`, item.id.toString()]);
      });
    });
  BlocklyUtil.groupMemberPool.set(id.toString(), groupList);
}
