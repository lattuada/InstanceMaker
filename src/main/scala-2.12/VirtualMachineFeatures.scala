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
object VirtualMachineFeatures {
  def apply (vmId: String): (Int, String) = vmId match {
    case name if name contains "medium" => 2 -> "Amazon"
    case name if name contains "2xlarge" => 16 -> "Amazon"
    case name if name contains "5xlarge" => 40 -> "Cineca"
    case name if name contains "xlarge" => 8 -> "Amazon"
    case name if name contains "large" => 4 -> "Amazon"
    case _ => throw new RuntimeException ("error: unrecognized VM type")
  }
}
