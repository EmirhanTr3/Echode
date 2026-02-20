package cat.emir.echode.arguments

import cat.emir.echode.Echode
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.concurrent.CompletableFuture

class LuaArgument : CustomArgumentType.Converted<String, String> {
    val plugin = Echode.instance
    val lua = plugin.engine.lua

    override fun convert(nativeType: String): String {
        return nativeType
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.greedyString()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining

        val expressionStart = findActiveExpressionStart(remaining)
        val rawPath = remaining.substring(expressionStart)

        val results = getNestedCompletions(rawPath)

        val lastSep = remaining.lastIndexOfAny(charArrayOf('.', ':'))
        val finalOffset = if (lastSep != -1 && lastSep >= expressionStart) {
            builder.start + (lastSep + 1)
        } else {
            builder.start + expressionStart
        }

        val subBuilder = builder.createOffset(finalOffset)

        results.forEach { (name, type) ->
            val suggestionName = when {
                type.startsWith("field-") -> type.substringAfter("field-")
                type != "any" -> "$name()"
                else -> name
            }

            subBuilder.suggest(suggestionName, MessageComponentSerializer.message().serialize(Component
                .text("returns ${type.split("-").last()}")
                .color(NamedTextColor.AQUA)
            ))
        }

        return builder.add(subBuilder).buildFuture()
    }

    fun getNestedCompletions(input: String): Map<String, String> {
        val cleanPath = stripParameters(input)

        val completions = mutableMapOf<String, String>()
        val parts = cleanPath.split(Regex("[.:]")).toMutableList()
        val lastPart = parts.removeAt(parts.size - 1).replace("()", "")

        val baseline = lua.top
        try {
            lua.getGlobal("_G")
            var currentClass: Class<*>? = null

            for (segment in parts) {
                if (segment.isBlank()) continue
                val isMethodCall = segment.contains("()")
                val name = segment.replace("()", "")

                if (currentClass != null) {
                    val method = currentClass.methods.find { it.name == name }
                    val field = try { currentClass.getField(name) } catch (e: Exception) { null }
                    currentClass = method?.returnType ?: field?.type
                    if (currentClass == null) return emptyMap()
                } else {
                    if (!moveToChild(name)) return emptyMap()
                    if (lua.isJavaObject(-1)) {
                        val obj = lua.toJavaObject(-1) ?: tryExtractFromMetatable()
                        currentClass = obj?.javaClass
                        if (isMethodCall && currentClass != null) {
                            currentClass = currentClass.methods.find { it.name == name }?.returnType
                        }
                    }
                }
            }

            if (currentClass != null) {
                currentClass.methods
                    .filter { it.name.startsWith(lastPart, ignoreCase = true) }
                    .forEach { completions[it.name] = it.returnType.simpleName }

                currentClass.fields
                    .filter { it.name.startsWith(lastPart, ignoreCase = true) }
                    .forEach { completions[it.name] = "field-${it.type.simpleName}" }
            } else if (lua.isTable(-1)) {
                val luaList = mutableListOf<String>()
                iterateLuaTable(lastPart, luaList)
                luaList.forEach { completions[it] = "any" }
            }
        } finally {
            lua.top = baseline
        }

        return completions
    }

    private fun findActiveExpressionStart(text: String): Int {
        var depth = 0
        for (i in text.indices.reversed()) {
            val c = text[i]
            if (c == ')') depth++
            if (c == '(') {
                if (depth > 0) depth--
                else return i + 1
            }
            if (depth == 0 && (c == ' ' || c == ',' || c == '=')) {
                return i + 1
            }
        }
        return 0
    }

    private fun stripParameters(input: String): String {
        val result = StringBuilder()
        var depth = 0
        for (i in input.indices) {
            val c = input[i]
            if (c == '(') {
                if (depth == 0) result.append("()")
                depth++
            } else if (c == ')') {
                if (depth > 0) depth--
            } else if (depth == 0) {
                result.append(c)
            }
        }
        return result.toString()
    }

    private fun moveToChild(segment: String): Boolean {
        if (lua.isJavaObject(-1)) {
            lua.getMetatable(-1)
            lua.push("__index")
            lua.getTable(-2)
            lua.remove(-2)
        }

        if (lua.isTable(-1)) {
            lua.push(segment)
            lua.getTable(-2)
            lua.remove(-2)
            return true
        }
        return false
    }

    private fun tryExtractFromMetatable(): Any? {
        if (lua.getMetatable(-1) != 0) {
            lua.push("__self")
            lua.getTable(-2)
            val obj = lua.toJavaObject(-1)
            lua.pop(2)
            return obj
        }
        return null
    }

    private fun iterateLuaTable(lastPart: String, completions: MutableList<String>) {
        lua.pushNil()
        while (lua.next(-2) != 0) {
            lua.pushValue(-2)
            if (lua.isString(-1)) {
                val key = lua.toString(-1)
                if (key != null && key.startsWith(lastPart, ignoreCase = true)) {
                    completions.add(key)
                }
            }
            lua.pop(2)
        }
    }
}