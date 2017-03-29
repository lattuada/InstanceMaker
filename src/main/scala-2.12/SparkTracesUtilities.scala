/* Copyright 2017 Eugenio Gianniti
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

trait SparkTracesUtilities {
  this: FileUtilities =>

  protected def copyTracesFiles(id: String, inputDirectory: File, outputDirectory: File): Unit = {
    val jobId = inputDirectory.getName

    inputDirectory.listFiles filter { _.isDirectory } foreach {
      vmDirectory =>
        val vmId = vmDirectory.getName
        val (_, provider) = VirtualMachineFeatures(vmId)

        vmDirectory.listFiles filter {
          file =>
            val name = file.getName
            ( name contains ".txt" ) || ( name contains ".lua" )
        } foreach {
          file =>
            val basename = file.getName
            val originalName = basename stripSuffix ".txt" stripSuffix ".lua"
            val extension = basename stripPrefix originalName
            val outFileName = s"$id${originalName}J$jobId$provider$vmId$extension"
            val copiedFile = new File(outputDirectory, outFileName)
            fileCopyingHelper(file, copiedFile)
        }
    }
  }
}
