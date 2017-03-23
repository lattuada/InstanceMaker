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
  private lazy val USAGE = """InstanceMaker directory classes concurrency deadline""".stripMargin

  private lazy val ERROR = "error: wrong input arguments"

  def main(args: Array[String]): Unit = {
    val test = args lengthCompare 4
    if (test != 0) {
      Console.err println USAGE
      System exit 2
    }
    else handleInputArguments(args)
  }

  private def handleInputArguments(args: Array[String]): Unit = {
    try {
      val inputDirectory = new File(args(0)).getAbsoluteFile
      val second = args(1).toInt
      val third = args(2).toInt
      val deadline = args(3).toDouble
      InstanceCreator(inputDirectory, second, third, deadline).create()
    } catch {
      case _: NumberFormatException =>
        Console.err println ERROR
        System exit 1
    }
  }
}
