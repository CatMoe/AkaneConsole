package catmoe.fallencrystal.akaneconsole.util

import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

object ConsoleLogger {
    data class CacheEntry(val message: String, var count: Int = 1)

    private const val expireTime = 5
    private val messageCache = Caffeine.newBuilder().expireAfterWrite(expireTime.toLong(), TimeUnit.SECONDS).build<String, CacheEntry>()

    private fun writeMessage(message: String) {
        val entry = messageCache.getIfPresent(message)
        if (entry != null) { entry.count++ } else { messageCache.put(message, CacheEntry(message)) }
    }

    private fun getMessageCount(message: String): Int {
        val entry = messageCache.getIfPresent(message)
        return entry?.count ?:0
    }

    fun logger(mode: Int, message: String) {
        val m = MessageUtil
        val spamLimit = 3
        val messageCount = getMessageCount(message)
        writeMessage(message)
        if (messageCount == spamLimit) { m.logWarn("检测到指定消息的垃圾邮件 将阻止发送. ($expireTime 秒内允许发送 $spamLimit 条消息)")}
        if (messageCount >= spamLimit) return
        if (mode == 1) { m.logInfo(message) }
        if (mode == 2) { m.logWarn(message) }
        if (mode != 1 && mode != 2) { throw Exception("Type $mode is unknown mode.") }
    }

}