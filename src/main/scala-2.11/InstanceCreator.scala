import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import it.polimi.diceH2020.SPACE4Cloud.shared.generators.InstanceDataGenerator
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData._

import scala.collection.convert.WrapAsJava

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
        WrapAsJava seqAsJavaList classList.toSeq
      }

      val integerSet = set map { _.toInt.asInstanceOf[java.lang.Integer] }
      val vmMap = vmTypes filterKeys integerSet
      instance setMapTypeVMs {
        WrapAsJava mapAsJavaMap vmMap.toMap
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
        WrapAsJava mapAsJavaMap profileMap
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
  def apply(directory: File, numClasses: Int, hUp: Int, deadline: Double) = {
    val directoryMap = retrieveDirectoryMap(directory)
    new InstanceCreator(directoryMap, directoryMap.keySet subsets numClasses, hUp, deadline)
  }
}
