package sh.sit.plp.color

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType
import sh.sit.plp.PlayerLocatorPlus
import java.util.*
import kotlin.jvm.optionals.getOrNull

class PlayerDataState : SavedData() {
    companion object {
        private val CODEC = CompoundTag.CODEC
            .fieldOf("players")
            .xmap({ playersNbt ->
                val ret = hashMapOf<UUID, PlayerData>()
                playersNbt.keySet().forEach { k ->
                    val playerNbt = playersNbt.getCompound(k).getOrNull()
                    val playerData = PlayerData(
                        customColor = playerNbt?.getInt("customColor")?.orElse(0xFFFFFF) ?: 0xFFFFFF,
                    )
                    ret[UUID.fromString(k)] = playerData
                }
                PlayerDataState().also {
                    it.players = ret
                }
            }, { state ->
                CompoundTag().also { ret ->
                    state.players.forEach { (k, v) ->
                        val playerNbt = CompoundTag()
                        playerNbt.putInt("customColor", v.customColor)
                        ret.put(k.toString(), playerNbt)
                    }
                }
            })
            .codec()

        private val TYPE = SavedDataType(
            "${PlayerLocatorPlus.MOD_ID}-player_data",
            ::PlayerDataState,
            CODEC,
            null,
        )

        fun of(server: MinecraftServer): PlayerDataState {
            return server.overworld().dataStorage.computeIfAbsent(TYPE)
        }
    }

    private var players = hashMapOf<UUID, PlayerData>()

    fun getPlayer(uuid: UUID): PlayerData {
        return players.getOrPut(uuid) {
            setDirty()
            PlayerData()
        }
    }
}
