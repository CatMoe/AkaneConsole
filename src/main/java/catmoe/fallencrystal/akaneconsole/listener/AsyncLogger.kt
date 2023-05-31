package catmoe.fallencrystal.akaneconsole.listener

import catmoe.fallencrystal.akaneconsole.util.ConsoleLogger
import catmoe.fallencrystal.akaneconsole.util.Version
import catmoe.fallencrystal.moefilter.api.event.EventListener
import catmoe.fallencrystal.moefilter.api.event.FilterEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncChatEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncPostLoginEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerConnectEvent
import catmoe.fallencrystal.moefilter.api.event.events.bungee.AsyncServerSwitchEvent
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.net.InetSocketAddress

class AsyncLogger : EventListener {
    private val prefix = "[AkaneConsole]"
    @FilterEvent
    fun onServerConnect(event: AsyncServerConnectEvent) {
        if (event.isConnected) {
            val player = event.player
            val target = event.server.name ?: return
            val displayName = getDisplayName(player)
            ConsoleLogger.logger(1, "$prefix [Server] $displayName &e已与服务器 $target 建立连接.")
        }
    }

    @FilterEvent
    fun onPostLogin(event: AsyncPostLoginEvent) {
        val player = event.player
        val version = Version.getVersion(player.pendingConnection.version)
        val from = player.pendingConnection.virtualHost.hostString
        val ipAddress = (event.player.socketAddress as InetSocketAddress).address.toString().replace("/", "")
        ConsoleLogger.logger(1, "$prefix [PostLogin] [$ipAddress] ${getDisplayName(player)} 从 $from 登录到了服务器. (版本 $version)")
    }

    @FilterEvent
    fun onChat(event: AsyncChatEvent) {
        val player = event.sender
        val message = event.message
        val server = player.server.info.name
        val cancelled = if (event.isCancelled) " &c[已取消事件]" else ""
        if (event.isProxyCommand) { ConsoleLogger.logger(1, "$prefix [ProxyCommand] [$server ${getDisplayName(player)} &7: &f$message") }
        if (event.isBackendCommand) { ConsoleLogger.logger(1, "$prefix [BackendCommand] [$server] ${getDisplayName(player)} &7: &f$message") }
        if (!event.isProxyCommand && !event.isBackendCommand) { ConsoleLogger.logger(1, "$prefix [Chat] [$server] ${getDisplayName(player)} &7: &f$message $cancelled") }
    }

    @FilterEvent
    fun switchServer(event: AsyncServerSwitchEvent) {
        if (event.from == null) return // 当玩家是直接连接到此服务器的时候
        val from = event.from!!.name
        val target = event.player.server.info.name
        val displayName = getDisplayName(event.player)
        ConsoleLogger.logger(1, "$prefix [Server] $displayName $from -> $target")
    }

    private fun getDisplayName(player: ProxiedPlayer): String {
        val display = DisplayCache.getDisplay(player.uniqueId)
        val prefix = display.displayPrefix
        val suffix = display.displaySuffix
        val name = player.name
        return "$prefix$name$suffix"
    }
}