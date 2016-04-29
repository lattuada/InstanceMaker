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

import com.fasterxml.jackson.databind.ObjectMapper
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.{Solution, SolutionPerJob}

import scala.collection.convert.WrapAsScala

class SolutionCreator(directories: Map[String, File], hUp: Int, vms: Int, deadline: Double)
  extends QueryData(directories, hUp, deadline) with FileUtilities {

  private val coresPerVm = 20

  private lazy val data = directories.keys map {
    query =>
      val instanceId = s"jmt_${query}_h${hUp}_vm${vms}_D$deadline"

      val solution = new Solution(instanceId)
      solution setGamma vms * coresPerVm

      val solutionPerJob = new SolutionPerJob()
      solutionPerJob setJob { jobClasses(query) }
      val (vmId, (profile, _)) = jobProfiles(query).head
      solutionPerJob setProfile profile
      val vm = WrapAsScala iterableAsScalaIterable
        vmTypes(query.toInt) find { _.getId equals vmId.getTypeVM }
      solutionPerJob setTypeVMselected vm.get
      solutionPerJob setNumCores coresPerVm
      solutionPerJob setNumberVM vms
      solutionPerJob setNumberUsers hUp
      solutionPerJob setDuration Double.NaN

      solution setSolutionPerJob solutionPerJob

      (instanceId, query, solution)
  }

  def create(): Unit = data foreach {
    case (id, query, solution) =>
      val outputDirectory = new File(id)
      outputDirectory.mkdir()
      copyTracesFiles(id, directories(query), outputDirectory)
      val mapper = new ObjectMapper
      val jsonContent = mapper writeValueAsString solution
      val jsonFile = new File(outputDirectory, s"$id.json")
      fileWritingHelper(jsonContent, jsonFile)
  }
}

object SolutionCreator extends DirectoryHelper {
  def apply(directory: File, hUp: Int, cores: Int, deadline: Double): SolutionCreator = {
    val directoryMap = retrieveDirectoryMap(directory)
    new SolutionCreator(directoryMap, hUp, cores, deadline)
  }
}
