package cat.emir.echode.arguments

import cat.emir.echode.Echode
import cat.emir.echode.script.EchodeScript
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

class ScriptArgument : CustomArgumentType.Converted<EchodeScript, String> {
    val plugin = Echode.instance!!

    val ERROR_SCRIPT_NOT_FOUND: DynamicCommandExceptionType = DynamicCommandExceptionType { script: Any? ->
        MessageComponentSerializer.message().serialize(Component.text("$script does not exist."))
    }

    override fun convert(nativeType: String): EchodeScript {
        val script = plugin.loader.scripts[Path.of(nativeType)] ?: throw ERROR_SCRIPT_NOT_FOUND.create(nativeType)

        return script
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.greedyString()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        plugin.loader.scripts.values
            .map { it.relativePath.toString() }
            .filter { it.contains(builder.remaining) }
            .forEach(builder::suggest)

        return builder.buildFuture()
    }
}