/* Copyright 2015-2016 Eugenio Gianniti
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

object ParameterRegex {
  val avgMap = """Avg Map: (\d+)""".r
  val maxMap = """Max Map: (\d+)""".r
  val avgReduce = """Avg Reduce: (\d+)""".r
  val maxReduce = """Max Reduce: (\d+)""".r
  val avgShuffle = """Avg Shuffle: (\d+)""".r
  val maxShuffle = """Max Shuffle: (\d+)""".r
}
