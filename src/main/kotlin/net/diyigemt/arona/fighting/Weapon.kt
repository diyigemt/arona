package net.diyigemt.arona.fighting

open class Weapon(
  private val star: Int = 0,
  private val level: Int = 0,
  private var ammo: Int = 0,
  private val hit: Int = 0,
  private var reload: Int = 0,
  private val damage: Int = 0,
  private val fireInterval: Int = 0,
  private var magazine: Int = 0,
  private val reloadInterval: Int = 0,
  var onwer: Character? = null
) {
  init {
    this.magazine = this.ammo
  }
  private var lastFire: Int = 0
  fun tick(enemies: List<Character>): Unit {
    if (this.reload-- <= 0) {
      this.lastFire = 0
      this.magazine = this.ammo
    } else {
      return
    }
    this.lastFire--
    if (this.lastFire <= 0) {
      this.fire(enemies)
    }
  }
  private fun fire(enemies: List<Character>): Unit {
    if (enemies.isEmpty()) return
    if (this.magazine <= 0) {
      this.reloading()
      return
    }
    val target = enemies[0]
    var health = target.health
    val defence = target.defence
    val cause = this.hit * this.damage - defence
    health -= cause
    println("${onwer?.name ?: "charter"} hit ${target.name} by $cause")
    this.lastFire = fireInterval
    this.magazine -= this.hit
  }
  private fun reloading(): Unit {
    this.reload = this.reloadInterval
  }
}