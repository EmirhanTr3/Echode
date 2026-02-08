package cat.emir.echode.effects

import cat.emir.echode.script.effect.EchodeEffect
import cat.emir.echode.script.effect.ExecutionContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit

class BroadcastEffect : EchodeEffect {
    override val pattern = Regex("""broadcast "(.*)"""")

    override fun execute(match: MatchResult, context: ExecutionContext) {
        val message = match.groupValues[1]
        Bukkit.broadcast(Component.text(message))
    }
}