package cat.emir.echode.script.effect

import cat.emir.echode.Echode
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player

class EffectRegistry(val plugin: Echode) {
    private val effects = mutableListOf<EchodeEffect>()

    fun register(vararg effects: EchodeEffect) {
        for (effect in effects) {
            this.effects.add(effect)
        }
    }

    fun findAndExecute(line: String, context: ExecutionContext) {
        val trimmed = line.trim()
        val processed = processPlaceholders(trimmed, context)

        for (effect in effects) {
            val match = effect.pattern.matchEntire(processed)
            if (match != null) {
                effect.execute(match, context)
                return
            }
        }
        plugin.logger.info("Unknown effect: $trimmed")
    }

    fun processPlaceholders(line: String, context: ExecutionContext): String {
        var processed = line

        Regex("""\$(\{[\w\s0-9]+}|[A-z0-9_]\w*)""").findAll(processed).forEach {
            processed = processed.replace(it.groupValues[0], processString(context[it.groupValues[1]]))
        }

        return processed
    }

    fun processString(value: Any?): String {
        return when (value) {
            is Player -> value.name
            is Component -> PlainTextComponentSerializer.plainText().serialize(value)
            else -> value.toString()
        }
    }
}