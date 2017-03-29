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
import java.io.File

import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.{ClassParameters, JobProfile, PublicCloudParameters}

import scala.io.Source
import scala.util.Random

sealed abstract class QueryData(directories: Map[String, File], hUp: Int, deadline: Double) extends JobData {

  lazy val jobClasses: Map[String, ClassParameters] = directories map {
    case (id, _) =>
      val jobClass = new ClassParameters
      jobClass setThink 1e4
      jobClass setPenalty Random.nextDouble * 21 + 15
      jobClass setD deadline
      jobClass setHup hUp
      val hLow = (hUp * 0.7).round
      jobClass setHlow hLow.toInt
      jobClass setM 1.0
      jobClass setV 0.0
      id -> jobClass
  }

  lazy val vmDirectories: Map[String, Array[File]] = directories map {
    case (id, directory) =>
      val types = directory.listFiles filter { _.isDirectory }
      id -> types
  }

  lazy val publicCloud: Map[String, Map[String, Map[String, PublicCloudParameters]]] = {
    vmDirectories map {
      case (id, types) =>
        val parametersMap = types map {
          vm =>
            val parameters = new PublicCloudParameters
            parameters setEta Random.nextDouble * 0.3 + 0.1
            parameters setR (Random nextInt 31) + 10

            val vmId = vm.getName
            val (_, provider) = VirtualMachineFeatures(vmId)
            provider -> Map(vm.getName -> parameters)
        }

        id -> collectSubMaps(parametersMap)
    }
  }

  protected def collectSubMaps [T] (nestedSeq: Traversable[(String, Map[String, T])]): Map[String, Map[String, T]] = {
    nestedSeq groupBy { _._1 } map {
      case (key, xs) =>
        val completeMap = xs map { _._2 } reduce { _ ++ _ }
        key -> completeMap
    }
  }
}


class HadoopQueryData(directories: Map[String, File], hUp: Int, deadline: Double)
  extends QueryData(directories: Map[String, File], hUp: Int, deadline: Double) {

  lazy val jobProfiles: Map[String, Map[String, Map[String, JobProfile]]] = {
    vmDirectories map {
      case (id, types) =>
        val profiles = types map {
          vm =>
            val vmId = vm.getName
            val (_, provider) = VirtualMachineFeatures(vmId)

            val profile = new JobProfile
            profile put ("sh1max", 0.0)

            Source.fromFile (new File (vm, "numTasks.txt")).getLines () foreach {
              case TaskNumberRegex.mapNumber (value) => profile put ("nm", value.toDouble)
              case TaskNumberRegex.rsNumber (value) => profile put ("nr", value.toDouble)
              case _ =>
            }

            Source.fromFile (new File (vm, "param.txt")).getLines () foreach {
              case ParameterRegex.avgMap (value) => profile put ("mavg", value.toDouble)
              case ParameterRegex.maxMap (value) => profile put ("mmax", value.toDouble)
              case ParameterRegex.avgReduce (value) => profile put ("ravg", value.toDouble)
              case ParameterRegex.maxReduce (value) => profile put ("rmax", value.toDouble)
              case ParameterRegex.avgShuffle (value) => profile put ("shtypavg", value.toDouble)
              case ParameterRegex.maxShuffle (value) => profile put ("shtypmax", value.toDouble)
              case _ =>
            }

            provider -> Map(vmId -> profile)
        }

        id -> collectSubMaps(profiles)
    }
  }
}
