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
import java.io.{BufferedWriter, File, FileWriter}

import scala.io.Source

trait FileUtilities {
  protected def fileCopyingHelper(input: File, output: File): Unit = {
    resource managed { new BufferedWriter(new FileWriter(output)) } foreach {
      writer =>
        Source.fromFile(input).getLines() filterNot { _ contains "#" } map {
          _.trim } filterNot { _.isEmpty } foreach { line => writer write s"$line\n" }
    }
  }

  protected def fileWritingHelper(content: String, output: File): Unit = {
    resource managed { new BufferedWriter(new FileWriter(output)) } foreach {
      writer =>
        writer write content
    }
  }

  protected def copyTracesFiles(id: String, inputDirectory: File, outputDirectory: File): Unit = {
    val jobId = inputDirectory.getName
    inputDirectory.listFiles filter { _.isDirectory } foreach {
      vmDirectory =>
        val vmId = vmDirectory.getName
        val (_, provider) = VirtualMachineFeatures(vmId)

        val mapFileName = s"${id}MapJ$jobId$provider$vmId.txt"
        val originalMapFile = new File(vmDirectory, "map.txt")
        val copiedMapFile = new File(outputDirectory, mapFileName)
        fileCopyingHelper(originalMapFile, copiedMapFile)

        val rsFileName = s"${id}RSJ$jobId$provider$vmId.txt"
        val originalRsFile = new File(vmDirectory, "rs.txt")
        val copiedRsFile = new File(outputDirectory, rsFileName)
        fileCopyingHelper(originalRsFile, copiedRsFile)
    }
  }
}
