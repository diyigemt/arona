import Blockly, { Block, FieldImage } from "blockly";
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

  static registerExtensions(name: string, extension: () => void) {
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

  static createCustomField(src: string, fn?: (p1: FieldImage) => unknown) {
    return new FieldImage(src, 15, 15, undefined, fn);
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
}

export const groups: [string, string][] = [];
export const friends: [string, string][] = [];

export function doFetchContacts() {
  fetchBotContacts()
    .then((res) => {
      res.data.groups.forEach((item) => {
        groups.push([`${item.name} (${item.id.toString()})`, item.id.toString()]);
        doFetchGroupMember(item.id);
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

function doFetchGroupMember(id: number) {
  const groupList: [string, string][] = [];
  service
    .raw<Friend[]>({
      url: `/contacts/${id}`,
      method: "POST",
    })
    .then((r) => {
      r.data.forEach((item) => {
        groupList.push([`${item.remark} (${item.id})`, item.id.toString()]);
      });
    });
  BlocklyUtil.groupMemberPool.set(id.toString(), groupList);
}
