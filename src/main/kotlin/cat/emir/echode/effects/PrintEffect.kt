package cat.emir.echode.effects

import cat.emir.echode.Echode
import cat.emir.echode.script.effect.EchodeEffect
import cat.emir.echode.script.effect.ExecutionContext

class PrintEffect : EchodeEffect {
    override val pattern = Regex("""print "(.*)"""")

    override fun execute(match: MatchResult, context: ExecutionContext) {
        val message = match.groupValues[1]
        Echode.instance!!.logger.info(message)
    }
}