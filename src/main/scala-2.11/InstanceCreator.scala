import java.io.{BufferedWriter, File, FileWriter}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import it.polimi.diceH2020.SPACE4Cloud.shared.generators.InstanceDataGenerator
import it.polimi.diceH2020.SPACE4Cloud.shared.inputData._

import scala.collection.convert.WrapAsJava
import scala.io.Source
import scala.util.Random

class InstanceCreator(sets: Iterator[Set[File]]) {
  private lazy val data = for (set <- sets) yield {
    val classes = set.map(x => x.getName -> x).toMap
    val instanceId = classes.keys.mkString

    val instance = InstanceDataGenerator build()
    instance setId instanceId
    instance setGamma {
      classes.size * 200
    }

    val classList = classes map {
      case (id, directory) =>
        val job = new JobClass
        job setId id.toInt
        job setHlow 5
        job setHup 10
        job setThink 1e4
        job setJob_penalty {
          Random nextInt 20 + 15
        }
        job setD {
          Random.nextDouble * 2e6 + 5e5
        }
        job
    }
    instance setLstClass {
      WrapAsJava seqAsJavaList classList.toSeq
    }

    val vmTypes = classes map {
      case (id, directory) =>
        val myTypes = directory.listFiles filter {
          _.isDirectory
        }
        id -> myTypes
    }

    val vmMap = vmTypes map {
      case (id, types) =>
        val typeList = types map {
          vm =>
            val vmType = new TypeVM
            vmType setId vm.getName
            vmType setEta {
              Random.nextDouble * 0.4
            }
            vmType setR {
              Random nextInt 40
            }
            vmType
        }
        id.toInt.asInstanceOf[java.lang.Integer] -> {
          WrapAsJava seqAsJavaList typeList
        }
    }
    instance setMapTypeVMs {
      WrapAsJava mapAsJavaMap vmMap.toMap
    }

    val profileMap = for (
      (id, types) <- vmTypes;
      vm <- types
    ) yield {
      val vmId = vm.getName
      val key = new TypeVMJobClassKey(id.toInt, vmId)
      val profile = new Profile

      val containers = vmId match {
        case name if name contains "small" => 2
        case name if name contains "medium" => 4
        case name if name contains "large" => 8
        case _ => Random nextInt 8 * 2
      }
      profile setCM containers
      profile setCR containers
      profile setNM 65
      profile setNR 35
      profile setSH1max 0.0

      val text = Source.fromFile(new File(vm, "param.txt")).getLines()
      text foreach {
        case ParameterRegex.avgMap(value) => profile setMavg value.toDouble
        case ParameterRegex.maxMap(value) => profile setMmax value.toDouble
        case ParameterRegex.avgReduce(value) => profile setRavg value.toDouble
        case ParameterRegex.maxReduce(value) => profile setRmax value.toDouble
        case ParameterRegex.avgShuffle(value) => profile setSHtypavg value.toDouble
        case ParameterRegex.maxShuffle(value) => profile setSHtypmax value.toDouble
        case _ =>
      }

      key -> profile
    }
    instance setMapProfiles {
      WrapAsJava mapAsJavaMap profileMap.toMap
    }

    (instanceId, set, instance)
  }

  def create(): Unit = {
    def fileCopyingHelper(input: File, output: File) = {
      val writer = new BufferedWriter(new FileWriter(output))
      Source.fromFile(input).getLines() filterNot { _ contains "#" } filterNot {
        _.isEmpty } map { _.trim } foreach {  line => writer write s"$line\n" }
      writer.close()
    }

    def fileWritingHelper(content: String, output: File) = {
      val writer = new BufferedWriter(new FileWriter(output))
      writer write content
      writer.close()
    }

    data foreach {
      case (id, dirs, instance) =>
        val outputDirectory = new File(id)
        outputDirectory.mkdir()
        dirs foreach {
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
  def apply(directory: File, numClasses: Int) = {
    val childDirectories = directory.listFiles filter { _.isDirectory }
    val sets = childDirectories.toSet subsets numClasses
    new InstanceCreator(sets)
  }
}
