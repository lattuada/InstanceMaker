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

object Main {
  private lazy val USAGE =
    """InstanceMaker -m|-s directory classes concurrency deadline
      |  -m, --mapreduce: process Hadoop traces with simple MapReduce jobs
      |  -s, --spark: process Spark traces
    """.stripMargin

  private lazy val ERROR = "error: wrong input arguments"

  def main(args: Array[String]): Unit = {
    val test = args lengthCompare 5
    if (test != 0) {
      Console.err println USAGE
      System exit 2
    }
    else handleInputArguments(args)
  }

  private def handleInputArguments(args: Array[String]): Unit = {
    try {
      val inputDirectory = new File(args(1)).getAbsoluteFile
      val classes = args(2).toInt
      val concurrency = args(3).toInt
      val deadline = args(4).toDouble

      val maybeCreator = args.head match {
        case "-h"|"--help" =>
          Console.out println USAGE
          None
        case "-m"|"--mapreduce" =>
          Some (InstanceCreator forHadoop (inputDirectory, classes, concurrency, deadline))
        case "-s"|"--spark" =>
          Some (InstanceCreator forSpark (inputDirectory, classes, concurrency, deadline))
      }

      maybeCreator foreach { _.create() }
    } catch {
      case _: NumberFormatException =>
        Console.err println ERROR
        System exit 1
    }
  }
}
