package net.diyigemt.arona.fighting

class FightManager(
  private val friends: MutableList<net.diyigemt.arona.fighting.Character>,
  private val enemies: MutableList<net.diyigemt.arona.fighting.Character>,
  private var gameTick: Int = 300
) {
  fun start(): Unit {
    while (friends.isNotEmpty() && enemies.isNotEmpty() && gameTick > 0) {
      friends.forEach {
        it.tick(enemies, friends)
      }
      enemies.forEach {
        it.tick(friends, enemies)
      }
      checkDeath()
      gameTick--
    }
    if (gameTick < 0) {
      println("time out")
    }
    if (friends.isNotEmpty()) {
      println("you win")
    } else {
      println("you lost")
    }
  }
  private fun checkDeath() {
    val death: MutableList<net.diyigemt.arona.fighting.Character> = mutableListOf()
    friends.forEach {
      if (it.health <= 0) {
        println("${it.name} death")
        death.add(it)
      }
    }
    friends.removeAll(death)
    death.clear()
    enemies.forEach {
      if (it.health <= 0) {
        println("${it.name} death")
        death.add(it)
      }
    }
  }
}