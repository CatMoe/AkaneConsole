package catmoe.fallencrystal.akaneconsole

import catmoe.fallencrystal.akaneconsole.displayname.LuckPermsListener
import catmoe.fallencrystal.akaneconsole.listener.EventLogger
import catmoe.fallencrystal.moefilter.util.message.MessageUtil
import catmoe.fallencrystal.akaneconsole.util.OriginalFilter
import catmoe.fallencrystal.moefilter.api.logger.LoggerManager
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

class AkaneConsole : Plugin() {

    override fun onEnable() {
        ProxyServer.getInstance().pluginManager.registerListener(this, EventLogger(this))
        MessageUtil.logInfo("已成功载入")
        LoggerManager.registerFilter(OriginalFilter())
        MessageUtil.logInfo("已成功与MoeFilter挂钩并注册控制台过滤器.")
        registerLuckPermsListener()
    }

    override fun onDisable() {
        MessageUtil.logWarn("卸载中..")
        ProxyServer.getInstance().pluginManager.unregisterListener(EventLogger(this))
        LoggerManager.unregisterFilter(OriginalFilter())
        MessageUtil.logInfo("监听器已卸载完成.")
    }

    private fun registerLuckPermsListener() {
        val luckPerms = LuckPermsProvider.get()
        luckPerms.eventBus.subscribe(this, UserDataRecalculateEvent::class.java, LuckPermsListener::ReCacheDisplayName)
    }
}
