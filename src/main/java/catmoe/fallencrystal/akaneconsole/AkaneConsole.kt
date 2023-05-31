package catmoe.fallencrystal.akaneconsole

import catmoe.fallencrystal.akaneconsole.listener.AsyncLogger
import catmoe.fallencrystal.akaneconsole.listener.EventLogger
import catmoe.fallencrystal.akaneconsole.util.OriginalFilter
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class AkaneConsole : Plugin() {
    private val prefix = "[AkaneConsole]"

    override fun onEnable() {
        ProxyServer.getInstance().pluginManager.registerListener(this, EventLogger(this))
        MessageUtil.logInfo("$prefix 已成功载入")
        LoggerManager.registerFilter(OriginalFilter())
        MessageUtil.logInfo("$prefix 已成功与MoeFilter挂钩并注册控制台过滤器.")
        EventManager.registerListener(AsyncLogger())
        MessageUtil.logInfo("$prefix 正在使用 MoeFilter异步事件.")
    }

    override fun onDisable() {
        MessageUtil.logWarn("$prefix 卸载中..")
        ProxyServer.getInstance().pluginManager.unregisterListener(EventLogger(this))
        LoggerManager.unregisterFilter(OriginalFilter())
        EventManager.unregisterListener(AsyncLogger())
        MessageUtil.logInfo("$prefix 监听器已卸载完成.")
    }
}
