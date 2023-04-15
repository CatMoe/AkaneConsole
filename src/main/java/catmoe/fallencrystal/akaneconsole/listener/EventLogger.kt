package catmoe.fallencrystal.akaneconsole.listener

import catmoe.fallencrystal.akaneconsole.util.MessageUtil
import catmoe.fallencrystal.akaneconsole.util.Version
import net.luckperms.api.LuckPermsProvider
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.*

import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.net.SocketAddress

class EventLogger : Listener{

    private fun socketToAddress(address: SocketAddress): String { return address.toString().replaceFirst("/(.*)[:]\\d+/", "$1") }

    private fun getDisplayName(playerName: String): String {
        val player = ProxyServer.getInstance().getPlayer(playerName)
        val name = player.name
        val luckpermsUser = LuckPermsProvider.get().userManager.getUser(player.uniqueId)
        val luckpermsPrefix = luckpermsUser?.cachedData?.metaData?.prefix
        val luckpermsSuffix = luckpermsUser?.cachedData?.metaData?.suffix
        val prefix = if (luckpermsPrefix.isNullOrEmpty()) {""} else {luckpermsPrefix}
        val suffix = if (luckpermsSuffix.isNullOrEmpty()) {""} else {luckpermsSuffix}
        return prefix + name + suffix
    }
    @EventHandler (priority = -127)
    fun preLogin(event: PostLoginEvent) {
        val player = event.player
        val version = Version.getVersion(player.pendingConnection.version)
        val from = player.pendingConnection.virtualHost.hostString
        val ipAddress = socketToAddress(player.socketAddress)
        val displayName = getDisplayName(player.name)
        MessageUtil.logInfo("[PostLogin] [$ipAddress] $displayName 从 $from 登录到了服务器. (版本 $version)")
    }
    @EventHandler (priority = 127)
    fun ping(event: ProxyPingEvent) {
        val version = Version.getVersion(event.connection.version)
        val ipAddress = socketToAddress(event.connection.socketAddress)
        if (ipAddress.contains("/127.0.0.1")) return
        if (event.connection.version.toString() == "-1") return
        if (event.connection.version.toString() == "0") return
        MessageUtil.logInfo("[Ping] $ipAddress Ping了一下服务器. (版本 $version)")
    }

    @EventHandler (priority =  120)
    fun chat(event: ChatEvent) {
        val chat = event.message
        val isCommand = event.isCommand
        val isProxyCommand = event.isProxyCommand
        val player = event.sender.toString()
        val displayName = getDisplayName(player)
        val server = ProxyServer.getInstance().getPlayer(player).server.info.name
        if (!isCommand && !isProxyCommand) { MessageUtil.logInfo("[Chat] [$server] $displayName : $chat") }
        if (isProxyCommand) {MessageUtil.logInfo("[ProxyCommand] [$server] $displayName : $chat"); return}
        if (isCommand) {MessageUtil.logInfo("[Command] [$server] $displayName : $chat")}
    }

    @EventHandler
    fun switchServer(event: ServerSwitchEvent) {
        if (event.from == null) return
        val from = event.from.name
        val target = event.player.server.info.name
        val player = event.player.name
        val displayName = getDisplayName(player)
        MessageUtil.logInfo("[Server] $displayName $from -> $target")
    }

    @EventHandler (priority = 127)
    fun serverKick(event: ServerKickEvent) {
        try {
            val player = event.player.name
            val displayName = getDisplayName(player)
            val from = event.kickedFrom.name
            val reason = event.kickReasonComponent
            MessageUtil.logInfo("[Server] $displayName 因 \"$reason\" 从 $from 服务器断开连接.")
        } catch (_: NullPointerException) {}
    }

    @EventHandler (priority = 127)
    fun serverDisconnect(event: ServerDisconnectEvent) {
        try {
            val player = event.player
            val target = event.target.name
            val displayName = getDisplayName(player.name)
            MessageUtil.logInfo("[Server] $displayName 主动与服务器 $target 断开了连接.")
        } catch (_: NullPointerException) {}
    }

    @EventHandler (priority = 127)
    fun serverConnected(event: ServerConnectedEvent) {
        val player = event.player
        val target = event.server.info.name
        val displayName = getDisplayName(player.name)
        MessageUtil.logInfo("[Server] $displayName 已与服务器 $target 建立连接.")
    }

    @EventHandler (priority = 127)
    fun playerDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player
        val displayName = getDisplayName(player.name)
        MessageUtil.logInfo("[Server] $displayName 已与BungeeCord断开连接.")
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
