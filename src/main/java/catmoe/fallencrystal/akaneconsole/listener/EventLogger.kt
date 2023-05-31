@file:Suppress("DEPRECATION")

package catmoe.fallencrystal.akaneconsole.listener

import catmoe.fallencrystal.akaneconsole.util.ConsoleLogger
import catmoe.fallencrystal.akaneconsole.util.Version
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

class EventLogger(private val plugin: Plugin) : Listener{

    private fun socketToAddress(address: SocketAddress): String { return (address as InetSocketAddress).toString() }

    private val dontLoggerCommand: List<String> = listOf(
        "/login",
        "/l",
        "/reg",
        "/register",
        "/unregister",
        "/premium",
        "/cracked",
        "/createpassword",
        "/changepassword",
        "/startsession"
    )

    private val proxy: ProxyServer = ProxyServer.getInstance()

    private val prefix = "[AkaneConsole]"

    private fun getDisplayName(uuid: UUID): String {
        val player = ProxyServer.getInstance().getPlayer(uuid).displayName
        val display = DisplayCache.getDisplay(uuid)
        return "${display.displayPrefix}$player${display.displaySuffix}"
    }

    @EventHandler (priority = -127)
    fun ping(event: ProxyPingEvent) {
        proxy.scheduler.runAsync(plugin) {
            val version = event.connection.version
            val versionConverted = Version.getVersion(version)
            val ipAddress = socketToAddress(event.connection.socketAddress)
            // 不记录本地Ping 避免出现本地测试Ping垃圾邮件或其它东西. 未知的版本也是一样
            if (ipAddress.contains("/127.0.0.1")) return@runAsync
            /*
            某些MCP Ping的协议通常为0或-1 我们不记录此类Ping 虽然它也很可能是某些攻击造成的
            当然 如果状态为null包 (NullPing Crasher) 应该会直接抛出Exception ——没什么可做的 安装一个好的反机器人.
             */
            if (version == 0 || version == -1) return@runAsync
            ConsoleLogger.logger(1, "$prefix [Ping] $ipAddress Ping了一下服务器. (版本 $versionConverted)")
        }
    }

    @EventHandler (priority = -127)
    fun serverKick(event: ServerKickEvent) {
        proxy.scheduler.runAsync(plugin) {
            try {
                event.player.name
                val displayName = getDisplayName(event.player.uniqueId)
                val from = event.kickedFrom.name
                val reason = event.kickReason
                ConsoleLogger.logger(1, "$prefix [Server] $displayName 因 \"$reason&r\" 从 $from 服务器断开连接.")
            } catch (_: NullPointerException) {}
        }
    }

    @EventHandler (priority = -127)
    fun serverDisconnect(event: ServerDisconnectEvent) {
        proxy.scheduler.runAsync(plugin) {
            try {
                val player = event.player
                val target = event.target.name
                val displayName = getDisplayName(player.uniqueId)
                ConsoleLogger.logger(1, "$prefix [Server] $displayName 主动与服务器 $target 断开了连接.")
            } catch (_: NullPointerException) {}
        }
    }

    @EventHandler (priority = -127)
    fun serverConnected(event: ServerConnectedEvent) {
        proxy.scheduler.runAsync(plugin) {
            val player = event.player
            val target = event.server.info.name
            val displayName = getDisplayName(player.uniqueId)
            ConsoleLogger.logger(1, "$prefix [Server] $displayName 已与服务器 $target 建立连接.")
        }
    }

    @EventHandler (priority = 127)
    fun playerDisconnect(event: PlayerDisconnectEvent) {
        // 此方法不可异步 否则抛出NullPointerException (玩家都要断开连接了你异步个啥啊)
        try {
            val player = event.player
            val displayName = getDisplayName(player.uniqueId)
            ConsoleLogger.logger(1, "$prefix [Server] $displayName 已与BungeeCord断开连接.")
        } catch (_: NullPointerException) { }
    }
}

