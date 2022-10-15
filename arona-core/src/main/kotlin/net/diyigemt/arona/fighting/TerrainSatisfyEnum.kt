package net.diyigemt.arona.fighting

enum class TerrainSatisfyEnum(
  val rate: Float,
  val coverBlock: Float
) {
  D(0.8F, 0F),
  C(0.9F, 0.15F),
  B(1.0F, 0.30F),
  A(1.1F, 0.45F),
  S(1.2F, 0.60F),
  SS(1.3F, 0.75F)
}