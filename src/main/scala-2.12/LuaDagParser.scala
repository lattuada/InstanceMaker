/* Copyright 2017 Eugenio Gianniti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object LuaDagParser {
  private lazy val stageRegex = """.*name\w*=\w*"(\w+?)".*post\w*=\w*\{(["\w,\s]*)}.*""".r
  private lazy val nameRegex = """"(\w+?)"""".r

  def apply (luaString: String): Map[String, Set[String]] = {
    val stagesStrings = luaString.trim stripPrefix "{" stripSuffix "};" split """}\s*,\s*\{"""
    val couples = stagesStrings map {
      case stageRegex(name, post) =>
        val successors = nameRegex findAllIn post map { _ stripPrefix "\"" stripSuffix "\"" }
        name -> successors.toSet
    }
    couples.toMap
  }
}
