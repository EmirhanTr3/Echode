package cat.emir.echode.script.effect

interface EchodeEffect {
    val pattern: Regex

    fun execute(match: MatchResult, context: ExecutionContext)
}

typealias ExecutionContext = Map<String, Any>