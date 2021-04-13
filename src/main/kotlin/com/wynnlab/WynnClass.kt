@file:JvmName("Classes")

package com.wynnlab

import com.wynnlab.spells.Spell
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerializable
import java.io.File
import java.util.logging.Level

data class WynnClass(
    val id: String,
    val item: Material,
    val itemDamage: Int,
    val metaStats: Tuple4<Int>,
    val invertedControls: Boolean,
    val spells: List<Spell>
) : ConfigurationSerializable {

    override fun serialize(): Map<String, Any> {
        val out = LinkedHashMap<String, Any>()

        out["id"] = id
        out["item"] = item.name
        if (itemDamage != 0) out["item_damage"] = itemDamage
        out["metaStats"] = mapOf("damage" to metaStats.v1, "defence" to metaStats.v2, "range" to metaStats.v3, "spells" to metaStats.v4)
        out["invertedControls"] = invertedControls
        out["spells"] = spells.map { it.serialize() }

        return out
    }

    companion object {
        @JvmStatic
        @Suppress("unused", "unchecked_cast")
        fun deserialize(map: Map<String, Any>): WynnClass {
            val id = map["id"] as String
            val item = Material.valueOf(map["item"] as String)
            val itemDamage = (map["item_damage"] as Number??: 0).toInt()
            val metaStats = map["metaStats"] as Map<String, Number>
            val invertedControls = map["invertedControls"] as Boolean
            val spells = map["spells"] as List<Spell>

            spellOrdinal = 0

            return WynnClass(id, item, itemDamage,
                Tuple4(metaStats["damage"]!!.toInt(), metaStats["defence"]!!.toInt(), metaStats["range"]!!.toInt(), metaStats["spells"]!!.toInt()),
                invertedControls, spells)
        }

        operator fun get(string: String) = classes[string]
    }
}

val classes = hashMapOf<String, WynnClass>()

internal var spellOrdinal = 0

fun loadClasses() {
    val classFolder = File(plugin.dataFolder, "classes")

    if (!classFolder.exists()) {
        plugin.logger.log(Level.WARNING, "No classes loaded")
        return
    }

    for (f in classFolder.listFiles { f, _ -> f.isDirectory } ?: return) {
        currentClassLoadFolder = f

        plugin.logger.log(Level.INFO, "Loading class ${f.name} ...")

        val configFile = File(f, "${f.name}.yml")
        val config = YamlConfiguration()
        config.load(configFile)
        val wynnClass = config.getSerializable("class", WynnClass::class.java) ?: continue
        classes[wynnClass.id] = wynnClass
    }

    //plugin.logger.log(Level.INFO, "Classes: $classes")
    //plugin.logger.log(Level.INFO, "Listeners: ${plugin.projectileHitListener.tags}")
}

data class Tuple4<T>(val v1: T, val v2: T, val v3: T, val v4: T)

internal lateinit var currentClassLoadFolder: File