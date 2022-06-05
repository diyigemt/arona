package net.diyigemt.arona.function;

import net.diyigemt.arona.function.group.ActivityFunction;
import net.mamoe.mirai.event.events.FriendEvent;
import net.mamoe.mirai.event.events.GroupEvent;

import java.util.HashMap;
import java.util.Map;

public class EventMap {
  public static final Map<String, EventFunction> GROUP_MAP = new HashMap<>();
  public static final Map<String, EventFunction> FRIEND_MAP = new HashMap<>();
  static {
    GROUP_MAP.put("测试", new ActivityFunction());
  }
}
