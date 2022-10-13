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
  private var lastFire: Int = 0
  private var isReload: Boolean = false
  init {
    this.magazine = this.ammo
    this.reload = this.reloadInterval
  }
  fun tick(enemies: List<Character>): Unit {
    if (this.isReload && this.reload-- <= 0) {
      this.isReload = false
      this.magazine = this.ammo
      onwer?.manager?.logEvent("${onwer?.name} reload complete")
    }
    if (this.isReload) return
    this.lastFire--
    if (this.lastFire <= 0) {
      this.fire(enemies)
      this.lastFire = this.fireInterval
    }
  }
  private fun fire(enemies: List<Character>): Unit {
    if (enemies.isEmpty()) return
    val target = enemies[0]
    val defence = target.defence
    val hits = IntArray(hit) { damage - defence}
    val cause = hits.reduce{ prv, cur -> prv + cur }
    target.health -= cause
    onwer?.manager?.logEvent("${onwer?.name ?: "charter"}(${onwer?.health}/${onwer?.maxHealth}) make ${hits.size} hit to ${target.name} cause ${hits.joinToString(",")} damage")
    this.magazine -= this.hit
    if (this.magazine <= 0) {
      this.reloading()
    }
  }
  private fun reloading(): Unit {
    onwer?.manager?.logEvent("${onwer?.name} is reloading")
    this.reload = this.reloadInterval
    this.isReload = true
  }
}