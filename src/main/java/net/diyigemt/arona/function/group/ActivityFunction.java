package net.diyigemt.arona.function.group;

import net.diyigemt.arona.function.EventFunction;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class ActivityFunction implements EventFunction<GroupMessageEvent> {
  @Override
  public void action(GroupMessageEvent event) {
    event.getGroup().sendMessage("爬");
  }
}
