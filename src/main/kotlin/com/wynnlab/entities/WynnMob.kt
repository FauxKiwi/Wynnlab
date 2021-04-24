package com.wynnlab.entities

import com.wynnlab.spells.MobSpell
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack

data class WynnMob(
    val name: String,
    val mobType: EntityTypes<out EntityCreature>,
    val ai: AI,
    val level: Int,
    val health: Int,
    val regen: Int,
    val damage: IntRange,
    val attackSpeed: Double,
    val projectile: Class<out Projectile>?,
    val speed: Double,
    val vision: Double,
    val invisible: Boolean = false,
    val burning: Boolean = false,
    val baby: Boolean = false,
    val defense: Double,
    val elementalDamage: Elemental<IntRange>?,
    val elementalDefense: Elemental<Int>?,
    val ambientSound: Sound?,
    val hurtSound: Sound?,
    val deathSound: Sound?,
    val kbResistance: Double,
    val equipment: Equipment,
    val spells: List<MobSpell>
) : ConfigurationSerializable {
    // Custom entity inner class
    private inner class C(location: Location) : EntityCreature(mobType, (location.world as CraftWorld).handle) {
        init {
            setLocation(location.x, location.y, location.z, location.yaw, location.pitch)

            customName = ChatComponentText("${this@WynnMob.name} §6[Lv. $level]")
            customNameVisible = true
        }

        override fun initPathfinder() {
            this@WynnMob.ai.initPathfinder(goalSelector, targetSelector, this)
        }
    }

    fun spawn(location: Location) {
        val entity = C(location)

        entity.spawnIn((location.world as CraftWorld).handle)
        (location.world as CraftWorld).handle.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    override fun serialize(): MutableMap<String, Any> {
        TODO("Not yet implemented")
    }

    companion object {
        @[JvmStatic Suppress("unused", "unchecked_cast")]
        fun deserialize(map: Map<String, Any>): WynnMob {
            TODO("Not yet implemented")
        }
    }

    data class Equipment(
        val mainHand: ItemStack?
    )

    enum class AI(val initPathfinder: (PathfinderGoalSelector, PathfinderGoalSelector, EntityCreature) -> Unit) {
        NONE({ g, t, e ->
            g.a(0, PathfinderGoalFloat(e))
        }),
        NO_ATTACK({ g, t, e ->
            g.a(1, PathfinderGoalRandomStroll(e, .5))
            g.a(2, PathfinderGoalLookAtPlayer(e, EntityPlayer::class.java, .5f))
            g.a(0, PathfinderGoalRandomLookaround(e))
        }), MELEE({ g, t, e ->

        }), RANGED({ g, t, e ->

        }), SUPPORT({ g, t, e ->

        })
    }

    data class Elemental<T>(
        val earth: T,
        val thunder: T,
        val water: T,
        val fire: T,
        val air: T,
    )
}

/*fun <T> Class<T>.getProtectedMethod(name: String, vararg parameters: Class<out Any?>): Method {
    val m = try {
        getMethod(name, *parameters)
    } catch (e: NoSuchMethodException) {
        var clazz: Class<in T> = this
        var method: Method? = null
        while (method == null) method = try {
            clazz.getDeclaredMethod(name, *parameters)
        } catch (e: NoSuchMethodException) {
            clazz = clazz.superclass ?: throw NoSuchMethodException(name)
            null
        }
        method
    }
    m.isAccessible = true
    return m
}*/