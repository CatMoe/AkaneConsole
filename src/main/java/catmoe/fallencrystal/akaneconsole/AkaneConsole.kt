package catmoe.fallencrystal.akaneconsole

import catmoe.fallencrystal.akaneconsole.listener.EventLogger
import catmoe.fallencrystal.akaneconsole.util.OriginalFilter
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class AkaneConsole : Plugin() {

    override fun onEnable() {
        ProxyServer.getInstance().pluginManager.registerListener(this, EventLogger(this))
        MessageUtil.logInfo("已成功载入")
        LoggerManager.registerFilter(OriginalFilter())
        MessageUtil.logInfo("已成功与MoeFilter挂钩并注册控制台过滤器.")
    }

    override fun onDisable() {
        MessageUtil.logWarn("卸载中..")
        ProxyServer.getInstance().pluginManager.unregisterListener(EventLogger(this))
        LoggerManager.unregisterFilter(OriginalFilter())
        MessageUtil.logInfo("监听器已卸载完成.")
    }
}
