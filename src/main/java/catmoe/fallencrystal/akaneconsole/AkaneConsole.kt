package catmoe.fallencrystal.akaneconsole

import catmoe.fallencrystal.akaneconsole.listener.EventLogger
import catmoe.fallencrystal.akaneconsole.util.OriginalFilter
import catmoe.fallencrystal.akaneconsole.util.MessageUtil
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import org.slf4j.LoggerFactory

class AkaneConsole : Plugin() {

    private val logger = LoggerFactory.getLogger(OriginalFilter::class.java)
    override fun onEnable() {
        ProxyServer.getInstance().logger.filter = OriginalFilter()
        ProxyServer.getInstance().pluginManager.registerListener(this, EventLogger(this))
        MessageUtil.logInfo("已成功载入. 这才是AkaneCh4n梦想中的控制台!")
    }

    override fun onDisable() {
        MessageUtil.logWarn("卸载中..")
        ProxyServer.getInstance().pluginManager.unregisterListener(EventLogger(this))
        MessageUtil.logInfo("监听器已卸载完成.")
    }
}
