import java.io.File

trait DirectoryHelper {
  protected def retrieveDirectoryMap(directory: File): Map[String, File] = {
    val childDirectories = directory.listFiles filter {
      _.isDirectory } map { x => x.getName -> x }
    childDirectories.toMap
  }
}
