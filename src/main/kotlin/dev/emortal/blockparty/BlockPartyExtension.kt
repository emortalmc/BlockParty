package dev.emortal.blockparty

import dev.emortal.blockparty.game.BlockPartyGame
import dev.emortal.immortal.config.GameOptions
import dev.emortal.immortal.game.GameManager
import dev.emortal.immortal.game.WhenToRegisterEvents
import net.minestom.server.extensions.Extension
import world.cepi.kstom.adventure.asMini

class BlockPartyExtension : Extension() {
    override fun initialize() {
        GameManager.registerGame<BlockPartyGame>(
            "blockparty",
            "<bold><rainbow>Block Party".asMini(),
            showsInSlashPlay = true,
            canSpectate = true,
            whenToRegisterEvents = WhenToRegisterEvents.GAME_START,
            GameOptions(
                maxPlayers = 20,
                minPlayers = 2,
                countdownSeconds = 20,
                showsJoinLeaveMessages = false
            )
        )

        logger.info("[${origin.name}] Party started!")
    }

    override fun terminate() {
        logger.info("[${origin.name}] Terminated")
    }
}