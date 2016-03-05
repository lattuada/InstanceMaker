import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.{Solution, SolutionPerJob}

import scala.collection.convert.WrapAsScala

class SolutionCreator(directories: Map[String, File], hUp: Int, vms: Int)
  extends QueryData(directories, hUp) with FileUtilities {

  private val coresPerVm = 20

  private lazy val data = directories.keys map {
    query =>
      val instanceId = s"jmt_${query}_h${hUp}_vm$vms"

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
  def apply(directory: File, hUp: Int, cores: Int): SolutionCreator = {
    val directoryMap = retrieveDirectoryMap(directory)
    new SolutionCreator(directoryMap, hUp, cores)
  }
}
