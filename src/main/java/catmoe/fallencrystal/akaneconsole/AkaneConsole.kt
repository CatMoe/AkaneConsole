package catmoe.fallencrystal.akaneconsole

import catmoe.fallencrystal.akaneconsole.config.Config
import catmoe.fallencrystal.akaneconsole.listener.Listener
import catmoe.fallencrystal.akaneconsole.util.OriginalFilter
import catmoe.fallencrystal.moefilter.api.event.EventManager
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import catmoe.fallencrystal.moefilter.util.message.v2.MessageUtil
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

@Suppress("unused")
class AkaneConsole : Plugin() {
    private val prefix = "[AkaneConsole]"

    override fun onLoad() {
        instance=this
    }

    private val config = Config(dataFolder)
    private val filter = OriginalFilter()
    private val pm = ProxyServer.getInstance().pluginManager

    override fun onEnable() {
        MessageUtil.logInfo("$prefix 已成功载入")
        LoggerManager.registerFilter(OriginalFilter())
        MessageUtil.logInfo("$prefix 已成功与MoeFilter挂钩并注册控制台过滤器.")
        EventManager.registerListener(this, Listener)
        pm.registerListener(this, Listener)
        MessageUtil.logInfo("$prefix 正在使用 MoeFilter 异步事件.")
    }

    override fun onDisable() {
        MessageUtil.logWarn("$prefix 卸载中..")
        LoggerManager.unregisterFilter(filter)
        EventManager.unregisterListener(Listener)
        pm.unregisterListener(Listener)
        MessageUtil.logInfo("$prefix 监听器已卸载完成.")
    }

    companion object {
        lateinit var instance: AkaneConsole
            private set
    }
}
