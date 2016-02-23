import java.io.{BufferedWriter, File, FileWriter}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import it.polimi.diceH2020.SPACE4Cloud.shared.generators.InstanceDataGenerator
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData._

import scala.collection.convert.WrapAsJava
import scala.io.Source
import scala.util.Random

class InstanceCreator(directories: Map[String, File],
                      sets: Iterator[Set[String]], hUp: Int) {
  private lazy val jobClasses = directories map {
    case (id, directory) =>
      val job = new JobClass
      job setId id.toInt
      job setThink 1e4
      job setJob_penalty (Random nextInt 21) + 15
      job setD Random.nextDouble * 2e6 + 5e5
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

  private lazy val vmTypes = vmDirectories map {
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

  private lazy val jobProfiles = vmDirectories map {
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

  private lazy val data = sets map {
    set =>
      val instanceId = set + s"h$hUp" mkString "_"

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

      val couples = (Seq[(TypeVMJobClassKey, (Profile, String))]() /: { set map jobProfiles })( _ ++ _ ).toMap
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

  def create(): Unit = {
    def fileCopyingHelper(input: File, output: File) = {
      val writer = new BufferedWriter(new FileWriter(output))
      Source.fromFile(input).getLines() filterNot { _ contains "#" } map {
        _.trim } filterNot { _.isEmpty } foreach { line => writer write s"$line\n" }
      writer.close()
    }

    def fileWritingHelper(content: String, output: File) = {
      val writer = new BufferedWriter(new FileWriter(output))
      writer write content
      writer.close()
    }

    data foreach {
      case (id, classes, instance) =>
        val outputDirectory = new File(id)
        outputDirectory.mkdir()
        classes map directories foreach {
          inputDirectory =>
            val jobId = inputDirectory.getName
            inputDirectory.listFiles filter { _.isDirectory } foreach {
              vmDirectory =>
                val vmId = vmDirectory.getName

                val mapFileName = s"${id}MapJ$jobId$vmId.txt"
                val originalMapFile = new File(vmDirectory, "map.txt")
                val copiedMapFile = new File(outputDirectory, mapFileName)
                fileCopyingHelper(originalMapFile, copiedMapFile)

                val rsFileName = s"${id}RSJ$jobId$vmId.txt"
                val originalRsFile = new File(vmDirectory, "rs.txt")
                val copiedRsFile = new File(outputDirectory, rsFileName)
                fileCopyingHelper(originalRsFile, copiedRsFile)
            }
        }
        val jsonFile = new File(outputDirectory, s"$id.json")
        val mapper = new ObjectMapper
        val module = new SimpleModule
        module addKeyDeserializer (classOf[TypeVMJobClassKey], TypeVMJobClassKey.getDeserializer)
        mapper registerModule module
        val serialized = mapper writeValueAsString instance
        fileWritingHelper(serialized, jsonFile)
    }
  }
}

object InstanceCreator {
  def apply(directory: File, numClasses: Int, hUp: Int) = {
    val childDirectories = directory.listFiles filter {
      _.isDirectory } map { x => x.getName -> x }
    val directoryMap = childDirectories.toMap
    new InstanceCreator(directoryMap, directoryMap.keySet subsets numClasses, hUp)
  }
}
