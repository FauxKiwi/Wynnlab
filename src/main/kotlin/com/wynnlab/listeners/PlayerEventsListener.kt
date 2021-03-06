package com.wynnlab.listeners

import com.wynnlab.Players
import com.wynnlab.api.*
import com.wynnlab.localization.Language
import com.wynnlab.pvp.Duels
import com.wynnlab.pvp.FFA
import com.wynnlab.spells.PySpell
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta

class PlayerEventsListener : BaseListener() {
    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player

        Players.preparePlayer(player)
        //e.joinMessage = "§7[§a+§7]§r ${player.prefix}${player.name}" 14b54a
        e.joinMessage(Component.text("[", TextColor.color(0x666666))
            .append(Component.text("+", TextColor.color(0x14b54a)))
            .append(Component.text("] ", TextColor.color(0x666666)))
            .append(Component.text(player.prefix + player.name)))
        //e.player.sendMessage("Locale: ${e.player.locale}")
        player.getWynnClass()?.let { c ->
            player.sendWynnMessage("messages.current_class", player.getLocalizedString("classes.$c.${if (player.isCloneClass) "cloneName" else "className"}"))
            player.sendWynnMessage("messages.class_change")
        } ?: run {
            player.sendWynnMessage("messages.no_class")
            player.sendWynnMessage("messages.class_select")
            player.performCommand("class")
        }
    }

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent) {
        val player = e.player

        //e.quitMessage = "§7[§c-§7]§r ${player.prefix}${player.name}" b52714
        e.quitMessage(Component.text("[", TextColor.color(0x666666))
            .append(Component.text("-", TextColor.color(0xb52714)))
            .append(Component.text("] ", TextColor.color(0x666666)))
            .append(Component.text(player.prefix + player.name)))

        // Remove player from activities
        Players.removePlayerFromActivities(player)
    }

    @EventHandler
    fun onPlayerChat(e: AsyncPlayerChatEvent) {
        val message = StringBuilder(e.message) //ChatColor.translateAlternateColorCodes('&', e.message)
        val selfMessage = StringBuilder(message)

        var mentions = false

        val matcher = mentionPattern.matcher(message)
        while (matcher.find()) {
            val name = message.substring(matcher.start(), matcher.end())
            Bukkit.getPlayer(name).let { p ->
                if (p != null && p.name == name) {
                    if (p.name != e.player.name) {
                        selfMessage.insert(matcher.start(), ChatColor.DARK_AQUA).insert(matcher.end() + 2, ChatColor.RESET)
                        p.sendMessage("${e.player.displayName}: §r${StringBuilder(message).insert(matcher.start(), ChatColor.AQUA).insert(matcher.end() + 2, ChatColor.RESET)}")
                        p.playSound(p.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, .7f)
                        e.recipients.remove(p)
                        mentions = true
                    }
                } else {
                    selfMessage.insert(matcher.start(), ChatColor.RED).insert(matcher.end() + 2, ChatColor.RESET)
                    mentions = true
                }
            }
        }

        if (mentions) {
            e.recipients.remove(e.player)
            e.player.sendMessage("${e.player.displayName}: §r$selfMessage")
        }

        e.message = message.toString()

        e.format = "%s: §r%s"
    }

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        e.keepInventory = true
        e.keepLevel = true
        e.setShouldDropExperience(false)
        e.droppedExp = 0

        e.entity.cancelSpell()

        if (e.entity.world.name.startsWith("Duel-Instance-"))
            Duels.playerDied(e.entity)

        e.deathMessage(e.entity.let { Language[it.locale()].getRandomMessage("death_messages", it.name) })
    }

    @EventHandler
    fun onPlayerToggleSneak(e: PlayerToggleSneakEvent) {
        val player = e.player
        if (e.isSneaking) {
            if (player.isGliding) {
                player.boostElytra(fireworks)

                PySpell.particle(player, player.location, Particle.CLOUD, 10, .5, .5, .5, .5)
                PySpell.particle(player, player.location, Particle.SQUID_INK, 10, .5, .5, .5, .5)
                PySpell.particle(player, player.location, Particle.LAVA, 10, .5, .5, .5, .5)

                PySpell.sound(player, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK , .3f, .6f)
                PySpell.sound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, .8f, 1f)
                PySpell.sound(player, Sound.ENTITY_BLAZE_SHOOT, 1f, 1.1f)
            }
        }
    }

    @EventHandler
    fun onPlayerChangeWorld(e: PlayerChangedWorldEvent) {
        val player = e.player

        player.cancelSpell()

        val from = e.from.name
        val to = e.player.world.name

        if (player.isOp)
            e.player.sendMessage("§7[World] $from >> $to")

        when {
            to == "FFA" -> FFA.onJoinWorld(player)
            from == "FFA" -> FFA.onLeaveWorld(player)
            from.startsWith("Duel-Instance-") -> Duels.playerLeft(player)
        }
    }

    companion object {
        val fireworks = ItemStack(Material.FIREWORK_ROCKET).metaAs<FireworkMeta> {
            power = 2
        }

        val mentionPattern = Regex("(?<=@)\\w{3,16}").toPattern()

        /*val chatPatternItalic = Regex("\\*.[^*]*\\*").toPattern()
        val chatPatternBold = Regex("\\*\\*.(?:[^*]|\\*[^*])*.\\*\\*").toPattern()
        val chatPatternBoldItalic = Regex("\\*\\*\\*.(?:[^*]|\\*[^*]|\\*\\*[^*])*.\\*\\*\\*").toPattern()
        val chatPatternUnderlined = Regex("_.[^_]*_").toPattern()
        val chatPatternStrikethrough = Regex("~~.(?:[^~]|~[^~])*.~~").toPattern()
        val chatPatternObfuscated = Regex("\\|\\|.(?:[^|]|\\|[^|])*.\\|\\|").toPattern()*/
    }
}