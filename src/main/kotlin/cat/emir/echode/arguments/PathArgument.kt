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
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.relativeTo

class PathArgument(val path: Path, val filter: (script: Path) -> Boolean = { true }, val maxDepth: Int = 3) : CustomArgumentType.Converted<Path, String> {
    val plugin = Echode.instance

    val ERROR_FILE_NOT_FOUND: DynamicCommandExceptionType = DynamicCommandExceptionType { file: Any? ->
        MessageComponentSerializer.message().serialize(Component.text("$file does not exist."))
    }

    override fun convert(nativeType: String): Path {
        val path = Echode.instance.scriptsPath / nativeType
        if (!path.exists() || !filter(path)) throw ERROR_FILE_NOT_FOUND.create(nativeType)

        return path
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.greedyString()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        Files.walk(path, maxDepth)
            .filter { it.extension == "lua" }
            .filter(filter)
            .map { it.relativeTo(path).toString()}
            .forEach(builder::suggest)

        return builder.buildFuture()
    }
}