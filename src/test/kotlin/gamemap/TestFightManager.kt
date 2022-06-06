package org.example.mirai.plugin.gamemap

import net.diyigemt.arona.fighting.*
import org.junit.jupiter.api.Test

class TestFightManager {
  @Test
  fun testBase(): Unit {
    val weaponA: Weapon = Weapon(
      1, 1,
      30, 3,
      3, 10,
      3, 0,
      5
    )
    val weaponB: Weapon = Weapon(
      1, 1,
      10, 1,
      3, 30,
      5, 0,
      10
    )
    val siroko = Character(
      "siroko",
      10, 1000, 10,
      10, 10, 10,
      10, 10, 10,
      10, 0, 10,
      10, 10,
      listOf<TerrainSatisfyEnum>(TerrainSatisfyEnum.B, TerrainSatisfyEnum.B, TerrainSatisfyEnum.B),
      weaponA, Position(10, 10)
    )
    val normalEnemy = Character(
      "normalEnemy",
      10, 500, 10,
      10, 10, 10,
      10, 10, 10,
      10, 0, 10,
      10, 10,
      listOf<TerrainSatisfyEnum>(TerrainSatisfyEnum.B, TerrainSatisfyEnum.B, TerrainSatisfyEnum.B),
      weaponB, Position(10, 10)
    )
    val friends: MutableList<net.diyigemt.arona.fighting.Character> = mutableListOf(
      siroko
    )
    val enemies: MutableList<net.diyigemt.arona.fighting.Character> = mutableListOf(
      normalEnemy
    )
    val manage: FightManager = FightManager(friends, enemies)
    manage.start()
  }
}
