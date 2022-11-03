package net.diyigemt.arona.interfaces

interface Initialize {

  val priority: Int
    get() = 100

  fun init()

}