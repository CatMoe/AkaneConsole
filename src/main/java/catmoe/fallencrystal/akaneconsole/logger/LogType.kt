package catmoe.fallencrystal.akaneconsole.logger

enum class LogType(@JvmField val path: String) {
    PING("ping"),
    JOIN("join"),
    JOIN_SERVER("join-server"),
    SWITCH_SERVER("switch-server"),
    LEAVE_SERVER("leave-server"),
    KICK("kick"),
    DISCONNECT("disconnect"),
    CLIENT_BRAND("client-brand"),
    CHAT_PROXYCOMMAND("chat.proxy-command"),
    CHAT_BACKENDCOMMAND("chat.backend-command"),
    CHAT("chat.default"),
    CHAT_CANCELLED("chat.cancelled"),
    FAILED_VERIFY("failed-verify"),
}