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
import java.io.File

import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.{JobClass, Profile, TypeVM, TypeVMJobClassKey}

import scala.collection.convert.WrapAsJava
import scala.io.Source
import scala.util.Random

abstract class QueryData(directories: Map[String, File], hUp: Int, deadline: Double) {
  protected lazy val jobClasses = directories map {
    case (id, directory) =>
      val job = new JobClass
      job setId id.toInt
      job setThink 1e4
      job setJob_penalty (Random nextInt 21) + 15
      job setD deadline
      job setHup hUp
      val hLow = (hUp * 0.7).round
      job setHlow hLow.toInt
      id -> job
  }

  private val vmDirectories = directories map {
    case (id, directory) =>
      val types = directory.listFiles filter { _.isDirectory }
      id -> types
  }

  protected lazy val vmTypes = vmDirectories map {
    case (id, types) =>
      val typeList = types map {
        vm =>
          val vmType = new TypeVM
          vmType setId vm.getName
          vmType setEta Random.nextDouble * 0.3 + 0.1
          vmType setR (Random nextInt 31) + 10
          vmType
      }
      id.toInt.asInstanceOf[java.lang.Integer] -> {
        WrapAsJava seqAsJavaList typeList
      }
  }

  protected lazy val jobProfiles = vmDirectories map {
    case (id, types) =>
      val profiles = types map {
        vm =>
          val vmId = vm.getName
          val key = new TypeVMJobClassKey(id.toInt, vmId)
          val profile = new Profile

          val (containers, provider) = vmId match {
            case name if name contains "medium" => 2 -> "Amazon"
            case name if name contains "2xlarge" => 16 -> "Amazon"
            case name if name contains "5xlarge" => 40 -> "Cineca"
            case name if name contains "xlarge" => 8 -> "Amazon"
            case name if name contains "large" => 4 -> "Amazon"
            case _ => throw new RuntimeException("error: unrecognized VM type")
          }
          profile setCM containers
          profile setCR containers
          profile setSH1max 0.0

          Source.fromFile(new File(vm, "numTasks.txt")).getLines() foreach {
            case TaskNumberRegex.mapNumber(value) => profile setNM value.toInt
            case TaskNumberRegex.rsNumber(value) => profile setNR value.toInt
            case _ =>
          }

          Source.fromFile(new File(vm, "param.txt")).getLines() foreach {
            case ParameterRegex.avgMap(value) => profile setMavg value.toDouble
            case ParameterRegex.maxMap(value) => profile setMmax value.toDouble
            case ParameterRegex.avgReduce(value) => profile setRavg value.toDouble
            case ParameterRegex.maxReduce(value) => profile setRmax value.toDouble
            case ParameterRegex.avgShuffle(value) => profile setSHtypavg value.toDouble
            case ParameterRegex.maxShuffle(value) => profile setSHtypmax value.toDouble
            case _ =>
          }

          key -> (profile, provider)
      }
      id -> profiles
  }
}
