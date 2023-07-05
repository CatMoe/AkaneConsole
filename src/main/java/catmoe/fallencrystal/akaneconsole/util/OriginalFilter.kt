package catmoe.fallencrystal.akaneconsole.util

import catmoe.fallencrystal.akaneconsole.config.Config
import catmoe.fallencrystal.akaneconsole.logger.LogType
import catmoe.fallencrystal.moefilter.api.logger.ILogger
import java.util.logging.LogRecord

class OriginalFilter : ILogger {

    private val original: List<String> = listOf(
        "{0} has connected",
        "{0} has pinged",
        "{0} has disconnected",
        "No client connected for pending server!",
        "[{0}] disconnected with: {1}",
        "{0} executed command: /{1}",
        "Error occurred processing connection for",
        "Error authenticating ",
        "with Minecraft.net",
        "Cloud verify username",
        "has been reloaded. This is NOT advisable and you will not be supported with any issues that arise! Please restart",
        "Plugin listener catmoe.fallencrystal."
    )

    override fun isLoggable(record: LogRecord?, isCancelled: Boolean): Boolean { if (record != null) { return !needFilter(record.message) }; return true }

    private fun needFilter(record: String): Boolean {
        if (record.contains("[AkaneConsole]")) return false
        mojangCatcher(record)
        for (filtered in original) {if(record.contains(filtered)) return true}
        return false
    }

    private fun mojangCatcher(log: String) {
        var logable = false
        if (log.contains("Error authenticating ") && log.contains(" with Minecraft.net")) logable=true
        if (log.contains("Could verify username!")) logable=true
        if (logable) { ConsoleLogger.logger(1, Config.instance.message[LogType.FAILED_VERIFY]!!) }
    }
}