package cat.emir.echode.commands

import cat.emir.echode.arguments.ScriptArgument
import cat.emir.echode.commandlib.PluginCommand
import cat.emir.echode.script.EchodeScript
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack

class MainCommand : PluginCommand() {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("echode") {
            requires { it.sender.hasPermission("echode.command") }
            subcommand("reload") {
                requires { it.sender.hasPermission("echode.command.reload") }
                argument("script", ScriptArgument()) {
                    executes(this@MainCommand::reload)
                }
                subcommand("all") {
                    executes(this@MainCommand::reloadAll)
                }
            }
        }
    }

    fun reloadAll(ctx: CommandContext<CommandSourceStack>): Int {
        plugin.loader.reloadScript(ctx.source.sender, null)

        return 1
    }

    fun reload(ctx: CommandContext<CommandSourceStack>): Int {
        val script = ctx.getArgument("script", EchodeScript::class.java)

        plugin.loader.reloadScript(ctx.source.sender, script)

        return 1
    }
}