package net.diyigemt.arona.interfaces

abstract class InitializedFunction {

  open val priority = 0
  abstract fun init()

}