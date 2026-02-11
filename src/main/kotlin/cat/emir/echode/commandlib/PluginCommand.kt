package cat.emir.echode.commandlib

import cat.emir.echode.Echode
import cat.emir.echode.commandlib.CommandLib.CommandAction
import cat.emir.echode.commandlib.CommandLib.CommandBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

abstract class PluginCommand {
    val plugin = Echode.instance!!
    
    fun command(
        name: String,
        setup: CommandBuilder.(LiteralArgumentBuilder<CommandSourceStack>) -> Unit
    ): LiteralArgumentBuilder<CommandSourceStack> {
        val builder = CommandBuilder(name)
        builder.setup(builder.node)
        return builder.node
    }

    /**
     * for java compatibility
     */
    fun command(name: String, setup: CommandAction<CommandBuilder>): LiteralArgumentBuilder<CommandSourceStack> {
        val builder = CommandBuilder(name)
        setup.accept(builder)
        return builder.node
    }

    abstract fun getCommand(): LiteralArgumentBuilder<CommandSourceStack>
}