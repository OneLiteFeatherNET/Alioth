package net.onelitefeather.alioth.extension

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.edit(consumer: (ItemMeta) -> Unit): ItemStack {
    editMeta(ItemMeta::class.java, consumer)
    return this
}

fun <T : ItemMeta> ItemStack.edit(metaClass: Class<T>, consumer: (T) -> Unit): ItemStack {
    editMeta(metaClass, consumer)
    return this
}

fun ItemMeta.legacyDisplay(value: String) {
    displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(value))
}

fun ItemMeta.legacyLore(vararg lore: String) {
    lore(lore.map { LegacyComponentSerializer.legacyAmpersand().deserialize(it) })
}