package net.diyigemt.arona.service

interface AronaCommandService: AronaService {
  val primaryName: String
  val secondaryNames: Array<out String>
}