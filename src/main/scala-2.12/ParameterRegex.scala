/* Copyright 2015-2017 Eugenio Gianniti
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
import scala.util.matching.Regex

object ParameterRegex {
  val avgMap: Regex = """Avg Map: (\d+)""".r
  val maxMap: Regex = """Max Map: (\d+)""".r
  val avgReduce: Regex = """Avg Reduce: (\d+)""".r
  val maxReduce: Regex = """Max Reduce: (\d+)""".r
  val avgShuffle: Regex = """Avg Shuffle: (\d+)""".r
  val maxShuffle: Regex = """Max Shuffle: (\d+)""".r
}
