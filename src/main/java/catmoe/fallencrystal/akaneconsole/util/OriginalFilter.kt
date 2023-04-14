package catmoe.fallencrystal.akaneconsole.util

import java.util.logging.Filter
import java.util.logging.LogRecord

class OriginalFilter : Filter {

    private val original: List<String> = listOf(
        "{0} has connected",
        "{0} has pinged",
        "{0} has disconnected",
        "No client connected for pending server!",
        "[{0}] disconnected with: {1}",
        "{0} executed command: /{1}",
        "Error occurred processing connection for",
        "Error authenticating",
        "has been reloaded. This is NOT advisable and you will not be supported with any issues that arise! Please restart"
    )

    override fun isLoggable(record: LogRecord?): Boolean { if (record != null) { return !needFilter(record.message) }; return true }

    private fun needFilter(record: String): Boolean {
        var match = false
        if (record.contains(MessageUtil.prefix())) return false
        exceptionCatcher(record)
        for (filtered in original) {if(record.contains(filtered)) match = true}
        return match
    }

    private fun exceptionCatcher(log: String) {
        val unreachableMojangSessionServer1 = "Error authenticating"
        val unreachableMojangSessionServer2 = "with minecraft.net"
        if (log.contains(unreachableMojangSessionServer1) && log.contains(unreachableMojangSessionServer2))
        {MessageUtil.logWarn("无法连接到认证服务器(sessionserver.mojang.com).")}
    }
}