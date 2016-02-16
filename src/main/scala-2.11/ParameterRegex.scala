object ParameterRegex {
  val avgMap = """Avg Map: (\d+)""".r
  val maxMap = """Max Map: (\d+)""".r
  val avgReduce = """Avg Reduce: (\d+)""".r
  val maxReduce = """Max Reduce: (\d+)""".r
  val avgShuffle = """Avg Shuffle: (\d+)""".r
  val maxShuffle = """Max Shuffle: (\d+)""".r
}
