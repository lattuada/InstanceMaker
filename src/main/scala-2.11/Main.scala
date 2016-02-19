import java.io.File

object Main {
  lazy val USAGE = "InstanceMaker directory classes concurrency"

  def main(args: Array[String]): Unit = {
    val test = args lengthCompare 3
    if (test != 0) Console.err println USAGE
    else handleInputArguments(args)
  }

  private def handleInputArguments(args: Array[String]) = {
    val inputDirectory = new File(args(0)).getAbsoluteFile
    val numClasses = args(1).toInt
    val hUp = args(2).toInt
    val creator = InstanceCreator(inputDirectory, numClasses, hUp)
    creator.create()
  }
}
