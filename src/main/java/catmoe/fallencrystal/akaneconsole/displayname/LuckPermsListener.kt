package catmoe.fallencrystal.akaneconsole.displayname

import net.luckperms.api.event.user.UserDataRecalculateEvent

object LuckPermsListener {
    @JvmStatic
    fun ReCacheDisplayName(event: UserDataRecalculateEvent) {
        val player = event.user.uniqueId
        DisplayName.onUpdateDisplayName(player)
    }
}