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
import java.util

import scala.collection.JavaConverters

object NestedJavaConverters {
  def threeNestedMaps [T] (outerMap: Map[String, Map[String, Map[String, T]]])
  : util.Map[String, util.Map[String, util.Map[String, T]]] = {
    JavaConverters mapAsJavaMap {
      outerMap map {
        case (id, midMap) =>
          id -> {
            JavaConverters mapAsJavaMap {
              midMap map {
                case (provider, innerMap) =>
                  provider -> {
                    JavaConverters mapAsJavaMap innerMap
                  }
              }
            }
          }
      }
    }
  }
}
