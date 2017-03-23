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
import com.fasterxml.jackson.databind.module.SimpleModule
import it.polimi.diceH2020.SPACE4Cloud.shared.generators.InstanceDataGenerator
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData.{Profile, TypeVMJobClassKey}

import scala.collection.JavaConverters

class InstanceCreator(directories: Map[String, File], sets: Iterator[Set[String]],
                      hUp: Int, deadline: Double)
  extends QueryData(directories, hUp, deadline) with FileUtilities {

  private lazy val data = sets map {
    set =>
      val instanceId = set.toSeq :+ s"h$hUp" :+ s"D$deadline" mkString "_"

      val instance = InstanceDataGenerator.build()
      instance setId instanceId
      instance setGamma set.size * hUp * 50

      val classList = set map jobClasses
      instance setLstClass {
        JavaConverters seqAsJavaList classList.toSeq
      }

      val integerSet = set map { _.toInt.asInstanceOf[java.lang.Integer] }
      val vmMap = vmTypes filterKeys integerSet
      instance setMapTypeVMs {
        JavaConverters mapAsJavaMap vmMap
      }

      val couples = (Seq[(TypeVMJobClassKey, (Profile, String))]()
        /: { set map jobProfiles })( _ ++ _ ).toMap
      val providers = couples.map{ case (_, (_, provider)) => provider }.toSet
      instance setProvider {
        providers.size match {
          case 1 => providers.head
          case _ => throw new RuntimeException("error: more than one provider in the same instance")
        }
      }
      val profileMap = couples map { case (key, (profile, _)) => key -> profile }
      instance setMapProfiles {
        JavaConverters mapAsJavaMap profileMap
      }

      (instanceId, set, instance)
  }

  def create(): Unit = data foreach {
    case (id, classes, instance) =>
      val outputDirectory = new File(id)
      outputDirectory.mkdir()
      classes map directories foreach { copyTracesFiles(id, _, outputDirectory) }
      val jsonFile = new File(outputDirectory, s"$id.json")
      val mapper = new ObjectMapper
      val module = new SimpleModule
      module addKeyDeserializer (classOf[TypeVMJobClassKey], TypeVMJobClassKey.getDeserializer)
      mapper registerModule module
      val serialized = mapper writeValueAsString instance
      fileWritingHelper(serialized, jsonFile)
  }
}

object InstanceCreator extends DirectoryHelper {
  def apply(directory: File, numClasses: Int, hUp: Int, deadline: Double): InstanceCreator = {
    val directoryMap = retrieveDirectoryMap(directory)
    new InstanceCreator(directoryMap, directoryMap.keySet subsets numClasses, hUp, deadline)
  }
}
