package catmoe.fallencrystal.akaneconsole.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

// from AkaneField

object MessageUtil {
    private var prefix = "[AkaneConsole] "
    @JvmStatic
    fun actionbar(p: ProxiedPlayer?, message: String) {
        try {
            if (p == null) {
                logError("Cannot send actionbar for console.")
                logError("Message: $message")
                throw RuntimeException("Cannot send actionbar for console")
            }
            p.sendMessage(ChatMessageType.ACTION_BAR, TextComponent(ca(message)))
        } catch (e: Exception) {
            logError("MessageSendUtil occurred exception")
            logError("Type: actionbar")
            logError("Message: $message")
            logError("Target: $p")
            throw RuntimeException("Cannot handle actionbar.")
        }
    }

    private fun rawchat(p: ProxiedPlayer?, message: String) {
        try {
            if (p == null) { logInfo(message); return }
            p.sendMessage(ChatMessageType.CHAT, TextComponent(ca(message)))
        } catch (e: Exception) {
            logError("MessageSendUtil occurred exception")
            logError("Type: Chat")
            logError("Message: $message")
            logError("Target: $p")
            throw RuntimeException("Cannot handle chat.")
        }
    }

    fun prefixchat(p: ProxiedPlayer?, message: String) { rawchat(p, prefix + message) }

    // Sender是专门为命令发送而准备的 皆在解决prefixchat和rawchat控制台的冲突
    @JvmStatic
    fun prefixsender(sender: CommandSender, message: String) { rawsender(sender, prefix + message) }

    private fun rawsender(sender: CommandSender, message: String) { if (sender !is ProxiedPlayer) { logInfo(message) } else { rawchat(sender, message) } }

    @JvmStatic
    fun fullTitle(p: ProxiedPlayer, title: String, subtitle: String, stay: Int, fadeIn: Int, fadeOut: Int) {
        try {
            val t = ProxyServer.getInstance().createTitle()
            t.title(TextComponent(ca(title)))
            t.subTitle(TextComponent(ca(subtitle)))
            t.stay(stay)
            t.fadeIn(fadeIn)
            t.fadeOut(fadeOut)
            t.send(p)
        } catch (e: Exception) {
            logError("MessageSendUtil occurred exception")
            logError("Type: title")
            logError("Title: $title")
            logError("Subtitle$subtitle")
            logError("Stay$stay, FadeIn$fadeIn, FadeOut$fadeOut")
            logError("Target: $p")
            throw RuntimeException("Cannot send fulltitle.")
        }
    }

    private fun broadcastRawChatPerms(message: String, permission: String?) {
        for (player in ProxyServer.getInstance().players) {
            if (player.hasPermission(permission)) {
                rawchat(player, message)
            }
        }
    }

    fun broadcastRawChat(message: String) { broadcastRawChatPerms(message, "") }

    private fun broadcastActionbarPerms(message: String, permission: String?) {
        for (player in ProxyServer.getInstance().players) {
            if (player.hasPermission(permission)) {
                actionbar(player, message)
            }
        }
    }

    fun broadcastActionbar(message: String) { broadcastActionbarPerms(message, "") }

    private fun broadcastPrefixChatPerms(message: String, permission: String?) {
        broadcastRawChatPerms(prefix + message, permission)
    }

    fun broadcastPrefixChat(message: String) { broadcastPrefixChatPerms(message, "") }

    private fun broadcastTitlePerms(
        title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int, permission: String?) {
        for (player in ProxyServer.getInstance().players) {
            if (player.hasPermission(permission)) {
                fullTitle(player, title, subtitle, stay, fadeIn, fadeOut)
            }
        }
    }

    fun broadcastTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) { broadcastTitlePerms(title, subtitle, fadeIn, stay, fadeOut, "") }

    @JvmStatic
    fun logInfo(message: String) { ProxyServer.getInstance().logger.info(ca(prefix + message)) }

    @JvmStatic
    fun prefix(): String {return prefix}

    fun logWarn(message: String) { ProxyServer.getInstance().logger.warning(ca(prefix + message)) }

    private fun logError(message: String) { ProxyServer.getInstance().logger.severe(ca(prefix + message)) }

    private fun ca(text: String?): String { return ChatColor.translateAlternateColorCodes('&', text) }
}