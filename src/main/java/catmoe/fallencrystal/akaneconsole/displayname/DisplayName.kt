package catmoe.fallencrystal.akaneconsole.displayname

import com.github.benmanes.caffeine.cache.Caffeine
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ProxyServer
import java.util.*

object DisplayName {
    private val cache = Caffeine.newBuilder().build<UUID, String>()

    fun getDisplayName(player: UUID): String {
        return cache.getIfPresent(player) ?: ({ setDisplayName(player); cache.getIfPresent(player) })!!
    }

    private fun setDisplayName(player: UUID) {
        try {
            val metaData = LuckPermsProvider.get().userManager.getUser(player)?.cachedData?.metaData
            val prefix = metaData?.prefix ?: ""
            val suffix = metaData?.suffix ?: ""
            cache.put(player, "$prefix${ProxyServer.getInstance().getPlayer(player).displayName}$suffix")
        } catch (ex: NullPointerException) { cache.put(player, "") }
    }

    fun onUpdateDisplayName(player: UUID) {
        cache.invalidate(player)
        setDisplayName(player)
    }
}