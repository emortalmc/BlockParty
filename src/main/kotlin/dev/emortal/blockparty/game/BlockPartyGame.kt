package dev.emortal.blockparty.game

import dev.emortal.immortal.config.GameOptions
import dev.emortal.immortal.game.Game
import dev.emortal.immortal.game.Team
import dev.emortal.immortal.util.MinestomRunnable
import dev.emortal.immortal.util.SuperflatGenerator
import kotlinx.coroutines.Runnable
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.minestom.server.attribute.Attribute
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerTickEvent
import net.minestom.server.instance.Instance
import net.minestom.server.instance.batch.AbsoluteBlockBatch
import net.minestom.server.instance.batch.BatchOption
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.scoreboard.Sidebar
import net.minestom.server.sound.SoundEvent
import net.minestom.server.utils.NamespaceID
import world.cepi.kstom.Manager
import world.cepi.kstom.event.listenOnly
import java.time.Duration
import kotlin.math.floor

class BlockPartyGame(gameOptions: GameOptions) : Game(gameOptions) {

    companion object {
        val concreteBlocks = ConcreteBlock.values()
        //val concreteMaterials = concreteBlocks.map { Material.fromNamespaceId(it.namespace())!! }

        val deathY = 0
        val mapSize = 32
        val lobbySpawnPos = Pos(10.5, 5.0, 0.5)
        val spawnPos = Pos(0.5, 7.0, 0.5)
    }

    private val aliveTeam = registerTeam(Team("alive", NamedTextColor.GREEN))
    private val deadTeam = registerTeam(Team("dead", NamedTextColor.RED))

    override var spawnPosition = lobbySpawnPos

    init {
        aliveTeam.scoreboardTeam.prefix = Component.text("ALIVE ", NamedTextColor.GREEN, TextDecoration.BOLD)
        deadTeam.scoreboardTeam.prefix = Component.text("DEAD ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
    }

    var roundSpeed = 6.0
    var roundNumber = 1

    var bossbar: BossBar? = null

    override fun playerJoin(player: Player) {
        sendMessage(
            Component.text()
                .append(Component.text("JOIN", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(Component.text(player.username, NamedTextColor.GREEN))
                .append(Component.text(" is ready to dance! ", NamedTextColor.GRAY))
                .append(Component.text("(${players.size}/${gameOptions.maxPlayers})", NamedTextColor.DARK_GRAY))

        )
    }

    override fun playerLeave(player: Player) {

    }

    override fun gameStarted() {
        bossbar = BossBar.bossBar(Component.empty(), 1f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)

        scoreboard?.createLine(Sidebar.ScoreboardLine(
            "roundNumber",
            Component.text()
                .append(Component.text("Round: ", NamedTextColor.GRAY))
                .append(Component.text(roundNumber, NamedTextColor.YELLOW))
                .build(),
            1
        ))
        scoreboard?.createLine(Sidebar.ScoreboardLine(
            "roundSpeed",
            Component.text()
                .append(Component.text("Round Speed: ", NamedTextColor.GRAY))
                .append(Component.text("${roundSpeed}s", NamedTextColor.YELLOW))
                .build(),
            2
        ))
        scoreboard?.createLine(Sidebar.ScoreboardLine("spacer", Component.empty(), 3))
        scoreboard?.createLine(Sidebar.ScoreboardLine(
            "dancersLeft",
            Component.text()
                .append(Component.text("Dancers Left: ", NamedTextColor.GRAY))
                .append(Component.text(players.size, NamedTextColor.RED))
                .build(),
            4
        ))

        scoreboard?.removeLine("infoLine")

        players.forEach {
            it.teleport(spawnPos)
            aliveTeam.add(it)
            it.getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.1f*1.4f
        }

        nextRound()
    }

    private fun nextRound() {
        players.forEach {
            it.inventory.clear()
        }
        scoreboard?.updateLineContent(
            "roundNumber",
            Component.text()
                .append(Component.text("Round: ", NamedTextColor.GRAY))
                .append(Component.text(roundNumber, NamedTextColor.YELLOW))
                .build()
        )
        scoreboard?.updateLineContent(
            "roundSpeed",
            Component.text()
                .append(Component.text("Round Speed: ", NamedTextColor.GRAY))
                .append(Component.text("${roundSpeed}s", NamedTextColor.YELLOW))
                .build()
        )


        val availableBlocks = mutableSetOf<ConcreteBlock>()

        val batch = AbsoluteBlockBatch(BatchOption().also { it.isFullChunk = true })
        for (x in -4..3) {
            for (z in -4..3) {
                val block = concreteBlocks.random()
                availableBlocks.add(block)

                for (x1 in 0..7) {
                    for (z1 in 0..7) {
                        batch.setBlock(x1+ (x*8), 5, z1 + (z * 8), block.block)
                    }
                }

            }
        }
        batch.apply(instance, null)

        val chosenBlock = availableBlocks.random()
        val chosenMaterial = chosenBlock.material

        val iterations = floor(roundSpeed).toInt()

        object : MinestomRunnable(coroutineScope = coroutineScope, delay = Duration.ofSeconds(4), repeat = Duration.ofSeconds(1), iterations = iterations) {

            override suspend fun run() {

                val currentIter = currentIteration.get()
                val secondsLeft = iterations - currentIter

                val boxComponent = Component.text("▇".repeat(secondsLeft), TextColor.color(chosenBlock.color))
                val component = Component.text()
                    .append(boxComponent)
                    .append(Component.text(" ${getColourFromBlock(chosenBlock).uppercase()} ", NamedTextColor.WHITE, TextDecoration.BOLD))
                    .append(boxComponent)
                    .build()
                sendActionBar(component)
                bossbar?.name(component)

                if (currentIter == 0) {

                    sendMessage(Component.text("Stand on ${getColourFromBlock(chosenBlock)}"))

                    val itemStack = ItemStack.builder(chosenMaterial)
                        //.displayName(C)
                        .build()
                    players.forEach {
                        it.inventory.setItemStack(4, itemStack)
                        it.showBossBar(bossbar!!)
                    }
                }

                if (secondsLeft <= 3) {
                    playSound(Sound.sound(SoundEvent.BLOCK_NOTE_BLOCK_HARP, Sound.Source.MASTER, 1f, 0.4f + (secondsLeft * 0.2f)), Sound.Emitter.self())
                }




            }

        }

        wait(Duration.ofSeconds(4 + iterations.toLong())) {
            playSound(Sound.sound(SoundEvent.ENTITY_ENDER_DRAGON_FLAP, Sound.Source.MASTER, 1f, 0.7f), Sound.Emitter.self())
            removeIncorrect(chosenBlock)

            players.forEach {
                it.hideBossBar(bossbar!!)
                it.sendActionBar(Component.empty())
            }
        }
        wait(Duration.ofSeconds(8 + iterations.toLong())) {
            roundNumber++
            if (roundNumber % 2 == 0 && iterations >= 1) {
                playSound(Sound.sound(SoundEvent.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1f, 1f), Sound.Emitter.self())
                roundSpeed -= 0.5
            }

            nextRound()
        }
    }

    private fun getColourFromBlock(block: ConcreteBlock): String =
        block.block.name()
            .replace("minecraft:", "")
            .replace("_concrete", "")
            .split("_")
            .map { it.replaceFirstChar(Char::uppercase) }
            .joinToString(separator = " ")

    private fun removeIncorrect(correctBlock: ConcreteBlock) {
        val batch = AbsoluteBlockBatch()
        for (x in -mapSize..mapSize) {
            for (z in -mapSize..mapSize) {
                if (instance.getBlock(x, 5, z) != correctBlock.block) {
                    batch.setBlock(x, 5, z, Block.AIR)
                }
            }
        }

        batch.apply(instance, null)
    }

    private fun wait(duration: Duration, runnable: Runnable) {
        object : MinestomRunnable(coroutineScope = coroutineScope, delay = duration) {
            override suspend fun run() {
                runnable.run()
            }
        }
    }

    fun strikeLightning(pos: Pos) {
        val lightningEntity = Entity(EntityType.LIGHTNING_BOLT)
        lightningEntity.setNoGravity(true)
        lightningEntity.scheduleRemove(Duration.ofSeconds(1))
        lightningEntity.setInstance(instance, pos)
    }

    override fun gameDestroyed() {

    }

    override fun registerEvents() = with(eventNode) {

        val deathMessages = setOf(
            "got distracted",
            "fell into the void",
            "couldn't hear over the music",
            "was eliminated",
            "couldn't find the colour in time"
        )
        listenOnly<PlayerTickEvent> {
            if (this.player.position.y < deathY) {
                if (!aliveTeam.players.contains(player)) return@listenOnly

                strikeLightning(player.position)
                deadTeam.add(player)
                aliveTeam.remove(player)
                player.gameMode = GameMode.SPECTATOR

                sendMessage(
                    Component.text()
                        .append(Component.text("☠", NamedTextColor.RED))
                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(player.username, NamedTextColor.RED))
                        .append(Component.text(" ${deathMessages.random()}", NamedTextColor.GRAY))
                )

                scoreboard?.updateLineContent(
                    "dancersLeft",
                    Component.text()
                        .append(Component.text("Dancers Left: ", NamedTextColor.GRAY))
                        .append(Component.text(aliveTeam.players.size, NamedTextColor.RED))
                        .build()
                )


            }
        }

    }

    override fun instanceCreate(): Instance {
        val fullbrightDimension = Manager.dimensionType.getDimension(NamespaceID.from("fullbright"))!!
        val newInstance = Manager.instance.createInstanceContainer(fullbrightDimension)
        newInstance.timeUpdate = null
        newInstance.timeRate = 0
        newInstance.setGenerator(SuperflatGenerator)

        return newInstance
    }

}