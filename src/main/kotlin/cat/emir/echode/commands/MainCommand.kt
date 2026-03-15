package cat.emir.echode.commands

import cat.emir.echode.arguments.LuaArgument
import cat.emir.echode.arguments.PathArgument
import cat.emir.echode.arguments.ScriptArgument
import cat.emir.echode.commandlib.PluginCommand
import cat.emir.echode.script.EchodeScript
import cat.emir.echode.script.RunLuaCode
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.nio.file.Path
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

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
            subcommand("disable") {
                requires { it.sender.hasPermission("echode.command.disable") }
                argument("script", ScriptArgument()) {
                    executes(::disable)
                }
            }
            subcommand("enable") {
                requires { it.sender.hasPermission("echode.command.enable") }
                argument("script", PathArgument(plugin.scriptsPath, { path ->
                    path.pathString.split("/", "\\").any { it.startsWith("-") }
                })) {
                    executes(::enable)
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

    fun disable(ctx: CommandContext<CommandSourceStack>): Int {
        val script = ctx.getArgument("script", EchodeScript::class.java)

        script.disable()

        ctx.source.sender.sendRichMessage("<aqua>[Echode] Disabled ${script.relativePath}")
        return 1
    }

    fun enable(ctx: CommandContext<CommandSourceStack>): Int {
        var path = ctx.getArgument("script", Path::class.java)

        val split = path.toString().split("/", "\\").toMutableList()
        if (split[split.lastIndex].contains("-")) {
            split[split.lastIndex] = split[split.lastIndex].replaceFirst("-", "")
            val newPath = Path.of(split.joinToString("/"))
            path.toFile().renameTo(newPath.toFile())
            path = newPath
        }

        val script = plugin.loader.loadScript(path)
        if (script == null) {
            ctx.source.sender.sendRichMessage("<red>[Echode] Script ${plugin.loader.scripts[path.relativeTo(plugin.scriptsPath)]} is already enabled.")
        } else {
            ctx.source.sender.sendRichMessage("<aqua>[Echode] Loaded script ${script.relativePath}")
        }

        return 1
    }
}