package net.diyigemt.arona.function;

import net.mamoe.mirai.event.Event;

public interface EventFunction<T extends Event> {
  default void action(T event) { throw new RuntimeException("please implements this function"); }
}
