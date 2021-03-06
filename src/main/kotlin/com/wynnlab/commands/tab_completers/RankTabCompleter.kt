package com.wynnlab.commands.tab_completers

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

object RankTabCompleter : BaseTabCompleter("rank") {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? = if (args.size == 1)
        completeWord(ranks, args[0])
    else null

    private val ranks = listOf("player", "vip", "vip+", "hero", "champion", "ct", "mod", "admin")
}