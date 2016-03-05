import java.io.File

object Main {
  private lazy val USAGE =
    """InstanceMaker -d directory classes concurrency deadline
      |InstanceMaker -s directory concurrency vms deadline""".stripMargin

  private lazy val ERROR = "error: unrecognized flag"

  def main(args: Array[String]): Unit = {
    val test = args lengthCompare 5
    if (test != 0) Console.err println USAGE
    else handleInputArguments(args)
  }

  private def handleInputArguments(args: Array[String]): Unit = {
    val inputDirectory = new File(args(1)).getAbsoluteFile
    val second = args(2).toInt
    val third = args(3).toInt
    val deadline = args(4).toDouble
    args.head match {
      case "-d" => InstanceCreator(inputDirectory, second, third, deadline).create()
      case "-s" => SolutionCreator(inputDirectory, second, third, deadline).create()
      case _ =>
        Console.err println ERROR
        Console.err println USAGE
    }
  }
}
