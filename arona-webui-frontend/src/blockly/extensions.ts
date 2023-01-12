import { fetchBotContacts } from "@/api/modules/contact";
import { warningMessage } from "@/utils/message";

// eslint-disable-next-line @typescript-eslint/no-empty-function
export default async function injectExtensions() {}

// eslint-disable-next-line import/no-mutable-exports
export let groups: [string, string][] = [];
// eslint-disable-next-line import/no-mutable-exports
export let friends: [string, string][] = [];

export async function doFetchContacts() {
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
