@file:Suppress("DEPRECATION")

package catmoe.fallencrystal.akaneconsole.listener

import catmoe.fallencrystal.akaneconsole.util.ConsoleLogger
import catmoe.fallencrystal.akaneconsole.util.MessageUtil
import catmoe.fallencrystal.akaneconsole.util.Version
import com.github.benmanes.caffeine.cache.Caffeine
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.*

import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

class EventLogger(private val plugin: Plugin) : Listener{

    private fun socketToAddress(address: SocketAddress): String { return address.toString().replaceFirst("/(.*)[:]\\d+/", "$1") }

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

    // Cache for displayName
    private val nameCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).weakKeys().build<String, String>()

    private fun getDisplayName(playerName: String): String {
        val cachedName = nameCache.getIfPresent(playerName)
        if (cachedName != null) {return cachedName}
        return try {
            val player = ProxyServer.getInstance().getPlayer(playerName)
            val name = player.name
            val luckpermsUser = LuckPermsProvider.get().userManager.getUser(player.uniqueId)
            val luckpermsPrefix = luckpermsUser?.cachedData?.metaData?.prefix
            val luckpermsSuffix = luckpermsUser?.cachedData?.metaData?.suffix
            val prefix = if (luckpermsPrefix.isNullOrEmpty()) {""} else {luckpermsPrefix}
            val suffix = if (luckpermsSuffix.isNullOrEmpty()) {""} else {luckpermsSuffix}
            val displayName = prefix + name + suffix
            nameCache.put(playerName, displayName)
            displayName
        } catch (e: NullPointerException) {
            playerName
        }
    }

    @EventHandler (priority = -127)
    fun preLogin(event: PostLoginEvent) {
        proxy.scheduler.runAsync(plugin) {
            val player = event.player
            val version = Version.getVersion(player.pendingConnection.version)
            val from = player.pendingConnection.virtualHost.hostString
            val ipAddress = socketToAddress(player.socketAddress)
            val displayName = getDisplayName(player.name)
            ConsoleLogger.logger(1, "[PostLogin] [$ipAddress] $displayName 从 $from 登录到了服务器. (版本 $version)")
        }
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
            ConsoleLogger.logger(1, "[Ping] $ipAddress Ping了一下服务器. (版本 $versionConverted)")
        }
    }

    @EventHandler (priority =  120)
    fun chat(event: ChatEvent) {
        proxy.scheduler.runAsync(plugin) {
            val chat = event.message
            val isCommand = event.isCommand
            val isProxyCommand = event.isProxyCommand
            val player = event.sender.toString()
            val displayName = getDisplayName(player)
            val server = proxy.getPlayer(player).server.info.name
            // 不要记录敏感命令
            for (command in dontLoggerCommand) { if (isCommand and chat.contains(command)) return@runAsync }
            if (!isCommand && !isProxyCommand) { ConsoleLogger.logger(1, "[Chat] [$server] $displayName : $chat") }
            if (isProxyCommand) {ConsoleLogger.logger(1, "[ProxyCommand] [$server] $displayName : $chat"); return@runAsync}
            if (isCommand) {ConsoleLogger.logger(1, "[Command] [$server] $displayName : $chat")}
        }
    }

    @EventHandler
    fun switchServer(event: ServerSwitchEvent) {
        proxy.scheduler.runAsync(plugin) {
            if (event.from == null) return@runAsync // 当玩家是直接连接到此服务器的时候
            val from = event.from.name
            val target = event.player.server.info.name
            val player = event.player.name
            val displayName = getDisplayName(player)
            ConsoleLogger.logger(1, "[Server] $displayName $from -> $target")
        }
    }

    @EventHandler (priority = -127)
    fun serverKick(event: ServerKickEvent) {
        proxy.scheduler.runAsync(plugin) {
            try {
                val player = event.player.name
                val displayName = getDisplayName(player)
                val from = event.kickedFrom.name
                val reason = event.kickReason
                ConsoleLogger.logger(1, "[Server] $displayName 因 \"$reason&r\" 从 $from 服务器断开连接.")
            } catch (_: NullPointerException) {}
        }
    }

    @EventHandler (priority = -127)
    fun serverDisconnect(event: ServerDisconnectEvent) {
        proxy.scheduler.runAsync(plugin) {
            try {
                val player = event.player
                val target = event.target.name
                val displayName = getDisplayName(player.name)
                ConsoleLogger.logger(1, "[Server] $displayName 主动与服务器 $target 断开了连接.")
            } catch (_: NullPointerException) {}
        }
    }

    @EventHandler (priority = -127)
    fun serverConnected(event: ServerConnectedEvent) {
        proxy.scheduler.runAsync(plugin) {
            val player = event.player
            val target = event.server.info.name
            val displayName = getDisplayName(player.name)
            ConsoleLogger.logger(1, "[Server] $displayName 已与服务器 $target 建立连接.")
        }
    }

    @EventHandler (priority = 127)
    fun playerDisconnect(event: PlayerDisconnectEvent) {
        // 此方法不可异步 否则抛出NullPointerException (玩家都要断开连接了你异步个啥啊)
        try {
            val player = event.player
            val displayName = getDisplayName(player.name)
            ConsoleLogger.logger(1, "[Server] $displayName 已与BungeeCord断开连接.")
        } catch (_: NullPointerException) { }
    }

    @EventHandler
    fun reload(event: ProxyReloadEvent) {
        if (event.sender is ProxiedPlayer) {
            val player = event.sender.name
            val displayName = getDisplayName(player)
            MessageUtil.logWarn("$displayName 尝试重新加载BungeeCord.")
        }
        MessageUtil.logWarn("不支持的操作. 请重启BungeeCord来完成重新加载的任务.")
        MessageUtil.logWarn("如果这不是您本人所为 建议将该命令列入黑名单!")
        ProxyServer.getInstance().stop()
    }
}

