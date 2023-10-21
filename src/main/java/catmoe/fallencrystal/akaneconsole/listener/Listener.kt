package catmoe.fallencrystal.akaneconsole.listener

import catmoe.fallencrystal.akaneconsole.AkaneConsole
import catmoe.fallencrystal.akaneconsole.config.Config
import catmoe.fallencrystal.akaneconsole.logger.LogType
import catmoe.fallencrystal.akaneconsole.util.ConsoleLogger
import catmoe.fallencrystal.akaneconsole.util.Version
import catmoe.fallencrystal.moefilter.api.user.displaycache.DisplayCache
import catmoe.fallencrystal.moefilter.network.bungee.util.bconnection.ConnectionUtil
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import catmoe.fallencrystal.moefilter.util.plugin.util.Scheduler
import catmoe.fallencrystal.translation.event.events.player.*
import catmoe.fallencrystal.translation.player.TranslatePlayer
import catmoe.fallencrystal.translation.player.bungee.BungeePlayer
import com.github.benmanes.caffeine.cache.Caffeine
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ProxyPingEvent
import net.md_5.bungee.api.event.ServerDisconnectEvent
import net.md_5.bungee.api.event.ServerKickEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.net.InetSocketAddress

object Listener : Listener {

    private val loggedBrand = Caffeine.newBuilder().build<ProxiedPlayer, Boolean>()

    private val messages = Config.instance.message
    private val scheduler = Scheduler(AkaneConsole.instance)

    private fun basicPlaceholder(message: String, player: ProxiedPlayer): String {
        var msg = message
        val connection = ConnectionUtil(player.pendingConnection)
        val map = mapOf(
            "%version%" to Version.getVersion(connection.version),
            "%host%" to connection.connection.virtualHost.hostString,
            "%name%" to player.name,
            "%displayname%" to getDisplayName(player),
            "%address%" to connection.inetAddress.hostAddress,
            "%server%" to try { player.server.info.name } catch (_: NullPointerException) { "" }
        )
        map.forEach { (placeholder, target) -> msg=msg.replace(placeholder, target) }
        return msg
    }

    private fun getDisplayName(player: ProxiedPlayer): String {
        val display = DisplayCache.getDisplay(player.uniqueId)
        return legacyToMiniMessage("${display.displayPrefix}${player.name}${display.displaySuffix}")
    }

    fun whenChat(event: PlayerChatEvent) {
        val sender = (event.player.upstream as BungeePlayer).player
        val cancelled = if (event.isCancelled()) messages[LogType.CHAT_CANCELLED]!! else ""
        if (event.isProxyCommand) {
            Config.instance.config.getStringList("messages.chat.ignore-commands").forEach { if (event.message.startsWith("/$it")) return }
            val message = basicPlaceholder(messages[LogType.CHAT_PROXYCOMMAND]!!, sender)
                .replace("%message%", event.message)
                .replace("%cancelled%", cancelled)
            ConsoleLogger.logger(1, message); return
        }
        if (event.isCommand()) {
            Config.instance.config.getStringList("messages.chat.ignore-commands").forEach { if (event.message.startsWith("/$it")) return }
            val message = basicPlaceholder(messages[LogType.CHAT_BACKENDCOMMAND]!!, sender)
                .replace("%message%", event.message)
                .replace("%cancelled%", cancelled)
            ConsoleLogger.logger(1, message); return
        }
        val message = basicPlaceholder(messages[LogType.CHAT]!!, sender)
            .replace("%message%", event.message)
            .replace("%cancelled%", cancelled)
        ConsoleLogger.logger(1, message); return
    }

    private fun getPlayer(player: TranslatePlayer): ProxiedPlayer {
        return (player.upstream as BungeePlayer).player
    }

    fun whenPostBrand(event: PlayerPostBrandEvent) {
        val player = getPlayer(event.player)
        if (loggedBrand.getIfPresent(player) == true) return
        val brand = event.brand
        ConsoleLogger.logger(1, basicPlaceholder(messages[LogType.CLIENT_BRAND]!!, player).replace("%brand%", brand))
        loggedBrand.put(player, true)
    }

    fun whenJoined(event: PlayerJoinEvent) {
        ConsoleLogger.logger(1, basicPlaceholder(messages[LogType.JOIN]!!, getPlayer(event.player)))
    }

    fun whenSwitchServer(event: PlayerSwitchServerEvent) {
        val player = (event.player.upstream as BungeePlayer).player
        val message = basicPlaceholder(messages[LogType.SWITCH_SERVER]!!, player)
            .replace("%from%", event.from.getName())
            .replace("%target%", event.to.getName())
        ConsoleLogger.logger(1, message)
    }

    fun whenServerConnected(event: PlayerConnectServerEvent) {
        if (event.isConnected) {
            val message =
                basicPlaceholder(messages[LogType.JOIN_SERVER]!!
                    .replace("%server%", event.server.getName()
                    ), getPlayer(event.player))
            ConsoleLogger.logger(1, message)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPing(event: ProxyPingEvent) {
        scheduler.runAsync {
            if (!event.connection.isConnected || event.connection.version == 0 || event.connection.version == -1) return@runAsync
            val message = Config.instance.message[LogType.PING]!!
                .replace("%version%", Version.getVersion(event.connection.version))
                .replace("%address%", (event.connection.socketAddress as InetSocketAddress).address.toString().replace("/", ""))
            ConsoleLogger.logger(1, message)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    @Suppress("DEPRECATION")
    fun whenKickBecauseBackend(event: ServerKickEvent) {
        scheduler.runAsync {
            try {
                val message = basicPlaceholder(messages[LogType.KICK]!!, event.player)
                    .replace("%reason%", legacyToMiniMessage(event.kickReason))
                    .replace("%from%", event.kickedFrom.name)
                ConsoleLogger.logger(1, message)
            } catch (ex: NullPointerException) {
                MessageUtil.logError("<red>[AkaneConsole] A internal error occurred when processing placeholder")
                ex.printStackTrace()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun whenDisconnectBackend(event: ServerDisconnectEvent) {
        scheduler.runAsync {
            try {
                val message = basicPlaceholder(messages[LogType.LEAVE_SERVER]!!, event.player).replace("%from%", event.target.name)
                ConsoleLogger.logger(1, message)
            } catch (ex: NullPointerException) {
                MessageUtil.logError("<red>[AkaneConsole] A internal error occurred when processing placeholder")
                ex.printStackTrace()
            }
        }
    }

    fun whenDisconnect(event: PlayerLeaveEvent) {
        try {
            val player = (event.player.upstream as BungeePlayer).player
            ConsoleLogger.logger(1, basicPlaceholder(messages[LogType.DISCONNECT]!!, player))
        } catch (ex: NullPointerException) {
            MessageUtil.logError("<red>[AkaneConsole] A internal error occurred when processing placeholder")
            ex.printStackTrace()
        }
        loggedBrand.invalidate(event.player)
    }

    private fun legacyToMiniMessage(legacy: String): String {
        val builder = MiniMessage.builder().strict(true).build()
        val message = LegacyComponentSerializer.legacySection().deserialize(ChatColor.translateAlternateColorCodes('&', legacy)) as Component
        return builder.serialize(message)
    }

}