package cat.emir.echode.commands

import cat.emir.echode.arguments.LuaArgument
import cat.emir.echode.arguments.ScriptArgument
import cat.emir.echode.commandlib.PluginCommand
import cat.emir.echode.script.EchodeScript
import cat.emir.echode.script.RunLuaCode
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

class MainCommand : PluginCommand() {
    override fun getCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return command("echode") {
            requires { it.sender.hasPermission("echode.command") }
            subcommand("reload") {
                requires { it.sender.hasPermission("echode.command.reload") }
                argument("script", ScriptArgument()) {
                    executes(::reload)
                }
                subcommand("all") {
                    executes(::reloadAll)
                }
            }
            subcommand("forcesave") {
                requires { it.sender.hasPermission("echode.command.forcesave") }
                executes(::forceSave)
            }
            subcommand("run") {
                requires { it.sender.hasPermission("echode.command.run") }
                argument("code", LuaArgument()) {
                    executes(::run)
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

    fun forceSave(ctx: CommandContext<CommandSourceStack>): Int {
        val count = plugin.variableManager.save()

        ctx.source.sender.sendRichMessage("<aqua>[Echode] Saved $count variables.")

        return 1
    }

    fun run(ctx: CommandContext<CommandSourceStack>): Int {
        val code = StringArgumentType.getString(ctx, "code")

        if (ctx.source.sender !is Player) {
            ctx.source.sender.sendRichMessage("<red>You cannot run this command as console.")
            return 1
        }

        ctx.source.sender.sendRichMessage("<gray>Running: $code")
        RunLuaCode(code, plugin.engine)
            .run(ctx.source.sender as Player)

        return 1
    }
}