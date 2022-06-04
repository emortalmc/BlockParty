package dev.emortal.blockparty.game

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.RGBLike
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material

enum class ConcreteBlock(val block: Block, val color: RGBLike) {
    WHITE(Block.WHITE_CONCRETE, NamedTextColor.WHITE),
    ORANGE(Block.ORANGE_CONCRETE, NamedTextColor.GOLD),
    MAGENTA(Block.MAGENTA_CONCRETE, NamedTextColor.LIGHT_PURPLE),
    LIGHT_BLUE(Block.LIGHT_BLUE_CONCRETE, NamedTextColor.BLUE),
    YELLOW(Block.YELLOW_CONCRETE, NamedTextColor.YELLOW),
    LIME(Block.LIME_CONCRETE, NamedTextColor.GREEN),
    PINK(Block.PINK_CONCRETE, TextColor.color(255, 50, 255)),
    DARK_GRAY(Block.GRAY_CONCRETE, NamedTextColor.DARK_GRAY),
    GRAY(Block.LIGHT_GRAY_CONCRETE, NamedTextColor.GRAY),
    CYAN(Block.CYAN_CONCRETE, NamedTextColor.AQUA),
    PURPLE(Block.PURPLE_CONCRETE, NamedTextColor.DARK_PURPLE),
    BLUE(Block.BLUE_CONCRETE, NamedTextColor.DARK_BLUE),
    BROWN(Block.BROWN_CONCRETE, TextColor.color(165, 42, 42)),
    RED(Block.RED_CONCRETE, NamedTextColor.RED),
    BLACK(Block.BLACK_CONCRETE, NamedTextColor.BLACK);

    val material get() = Material.fromNamespaceId(block.namespace())!!
}