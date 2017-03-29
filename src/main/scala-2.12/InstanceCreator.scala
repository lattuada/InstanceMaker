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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import it.polimi.diceH2020.SPACE4Cloud.shared.inputDataMultiProvider.{ClassParametersMap, InstanceDataMultiProvider, JobProfilesMap, PublicCloudParametersMap}

import scala.collection.JavaConverters

sealed abstract class InstanceCreator(directories: Map[String, File], sets: Iterator[Set[String]],
                                      hUp: Int, deadline: Double, private val jobData: JobData) extends FileUtilities {

  private lazy val data = sets map {
    set =>
      val instanceId = set.toSeq :+ s"h$hUp" :+ s"D$deadline" mkString "_"

      val instance = new InstanceDataMultiProvider
      instance setId instanceId

      val classMap = jobData.jobClasses filterKeys set
      val parametersMap = new ClassParametersMap (
        JavaConverters mapAsJavaMap classMap
      )
      instance setMapClassParameters parametersMap

      val filteredProfiles = jobData.jobProfiles filterKeys set
      val javaProfiles = NestedJavaConverters threeNestedMaps filteredProfiles
      val jobProfilesMap = new JobProfilesMap (javaProfiles)
      instance setMapJobProfiles jobProfilesMap

      val filteredPublicCloud = jobData.publicCloud filterKeys set
      val javaPublicCloud = NestedJavaConverters threeNestedMaps filteredPublicCloud
      val publicCloudParametersMap = new PublicCloudParametersMap (javaPublicCloud)
      instance setMapPublicCloudParameters publicCloudParametersMap

      (instanceId, set, instance)
  }

  def create(): Unit = data foreach {
    case (id, classes, instance) =>
      val outputDirectory = new File(id)
      outputDirectory.mkdir()

      classes map directories foreach {
        copyTracesFiles(id, _, outputDirectory)
      }

      val mapper = new ObjectMapper
      val module = new Jdk8Module
      mapper registerModule module

      val jsonWriter = mapper.writerWithDefaultPrettyPrinter
      val serialized = jsonWriter writeValueAsString instance
      val jsonFile = new File(outputDirectory, s"$id.json")
      fileWritingHelper(serialized, jsonFile)
  }
}


class HadoopInstanceCreator(directories: Map[String, File], sets: Iterator[Set[String]],
                            hUp: Int, deadline: Double, data: HadoopQueryData)
  extends InstanceCreator(directories, sets, hUp, deadline, data) with HadoopTracesUtilities


object InstanceCreator extends DirectoryHelper {
  def forHadoop (directory: File, numClasses: Int, hUp: Int, deadline: Double): InstanceCreator = {
    val directoryMap = retrieveDirectoryMap(directory)
    val queryData = new HadoopQueryData(directoryMap, hUp, deadline)
    new HadoopInstanceCreator(directoryMap, directoryMap.keySet subsets numClasses, hUp, deadline, queryData)
  }
}
