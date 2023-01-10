import { Block, Extensions, FieldDropdown } from "blockly";
import { fetchBotContacts } from "@/api/modules/contact";
import { warningMessage } from "@/utils/message";

export default async function injectExtensions() {
  await doFetchContacts();
  Extensions.register("groups_extension", groupsExtension);
  Extensions.register("friends_extension", friendsExtension);
}

export const groups: [string, string][] = [];
export const friends: [string, string][] = [];

async function doFetchContacts() {
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

function groupsExtension(this: Block) {
  this.getInput("IDInput")?.appendField(
    new FieldDropdown(() => {
      return groups;
    }),
    "groupNumber",
  );
}

function friendsExtension(this: Block) {
  this.getInput("sender_value_input")?.appendField(
    new FieldDropdown(() => {
      return friends;
    }),
    "friendNumber",
  );
}
