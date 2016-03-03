import java.io.File

object Main {
  private lazy val USAGE =
    """InstanceMaker -d directory classes concurrency
      |InstanceMaker -s directory concurrency cores""".stripMargin

  private lazy val ERROR = "error: unrecognized flag"

  def main(args: Array[String]): Unit = {
    val test = args lengthCompare 4
    if (test != 0) Console.err println USAGE
    else handleInputArguments(args)
  }

  private def handleInputArguments(args: Array[String]): Unit = {
    val inputDirectory = new File(args(1)).getAbsoluteFile
    val second = args(2).toInt
    val third = args(3).toInt
    args.head match {
      case "-d" => InstanceCreator(inputDirectory, second, third).create()
      case "-s" => SolutionCreator(inputDirectory, second, third).create()
      case _ =>
        Console.err println ERROR
        Console.err println USAGE
    }
  }
}
