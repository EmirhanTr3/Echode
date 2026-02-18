package cat.emir.echode.variables.serializers

import cat.emir.echode.variables.VariableSerializer
import org.bukkit.inventory.ItemStack

class ItemSerializer : VariableSerializer<ItemStack>() {
    override fun type(): Class<ItemStack> {
        return ItemStack::class.java
    }

    override fun deserialize(source: ByteArray): ItemStack {
        return ItemStack.deserializeBytes(source)
    }

    override fun serialize(source: ItemStack): ByteArray {
        return source.serializeAsBytes()
    }
}