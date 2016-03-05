import java.io.{BufferedWriter, File, FileWriter}

import scala.io.Source

trait FileUtilities {
  protected def fileCopyingHelper(input: File, output: File) = {
    val writer = new BufferedWriter(new FileWriter(output))
    Source.fromFile(input).getLines() filterNot { _ contains "#" } map {
      _.trim } filterNot { _.isEmpty } foreach { line => writer write s"$line\n" }
    writer.close()
  }

  protected def fileWritingHelper(content: String, output: File) = {
    val writer = new BufferedWriter(new FileWriter(output))
    writer write content
    writer.close()
  }

  protected def copyTracesFiles(id: String, inputDirectory: File,
                                outputDirectory: File) = {
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
}
