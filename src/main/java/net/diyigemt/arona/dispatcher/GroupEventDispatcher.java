package net.diyigemt.arona.dispatcher;

import net.diyigemt.arona.function.EventFunction;
import net.diyigemt.arona.function.EventMap;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import java.util.function.Consumer;

public final class GroupEventDispatcher implements Consumer<GroupMessageEvent> {
  @Override
  public void accept(GroupMessageEvent event) {
    String rowCommand = event.getMessage().contentToString();
    if (!rowCommand.startsWith("/")) return;
    String[] command = rowCommand.replaceFirst("/", "").split(" ");
    String commandName = command[0];
    EventFunction function = EventMap.GROUP_MAP.get(commandName);
    if (function != null) {
      function.action(event);
    }
  }
}
