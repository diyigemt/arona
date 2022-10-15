package net.diyigemt.arona.util.scbaleDB.factories

/**
 *@Author hjn
 *@Create 2022/8/20
 */
abstract class BaseFactory {
  fun getValueById(id : Int) : String?{
    for (member in this::class.members){
      if (member.name == id.toString()) return member.call(this).toString()
    }

    return null
  }
}