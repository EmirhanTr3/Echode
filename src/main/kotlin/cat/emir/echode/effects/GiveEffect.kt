package cat.emir.echode.effects

import cat.emir.echode.script.effect.EchodeEffect
import cat.emir.echode.script.effect.ExecutionContext
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GiveEffect : EchodeEffect {
    override val pattern = Regex("""give ([0-9]+)? (\w) to player""")

    override fun execute(match: MatchResult, context: ExecutionContext) {
        val amount = match.groupValues[1].toIntOrNull() ?: 1
        val material = Material.valueOf(match.groupValues[2].uppercase())
        val player = context.get("player") as Player

        player.inventory.addItem(ItemStack(material, amount))
    }
}