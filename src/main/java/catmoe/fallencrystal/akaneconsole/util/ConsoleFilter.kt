package catmoe.fallencrystal.akaneconsole.util

import java.util.logging.Filter
import java.util.logging.LogRecord

class ConsoleFilter : Filter {

    private val original: List<String> = listOf(
        "InitialHandler has connected",
        "InitialHandler has pinged",
        "No client connected for pending server!",
        " disconnected with: ",
        "Error occurred processing connection for",
        " <-> ServerConnector",
        " <-> DownstreamBridge <->",
        "io.netty.channel.ConnectTimeoutException: connection timed out: sessionserver.mojang.com",
        "has been reloaded. This is NOT advisable and you will not be supported with any issues that arise! Please restart"
    )

    override fun isLoggable(record: LogRecord?): Boolean { if (record != null) { return !needFilter(record.message) }; return true }

    private fun needFilter(record: String): Boolean {
        var match = false
        if (record.contains(MessageUtil.prefix())) return false
        for (filtered in original) {if(record.contains(record)) exceptionCatcher(record); match = true}
        return match
    }

    private fun exceptionCatcher(log: String) {
        val unreachableMojangSessionServer = "io.netty.channel.ConnectTimeoutException: connection timed out: sessionserver.mojang.com"
        if (log.contains(unreachableMojangSessionServer)) {MessageUtil.logWarn("[PreLogin] 无法连接到认证服务器(sessionserver.mojang.com).")}
    }
}