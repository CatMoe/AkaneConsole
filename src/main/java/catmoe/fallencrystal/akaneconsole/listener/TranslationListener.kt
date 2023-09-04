package catmoe.fallencrystal.akaneconsole.listener

import catmoe.fallencrystal.translation.event.EventListener
import catmoe.fallencrystal.translation.event.annotations.AsynchronousHandler
import catmoe.fallencrystal.translation.event.annotations.EventHandler
import catmoe.fallencrystal.translation.event.annotations.HandlerPriority
import catmoe.fallencrystal.translation.event.events.player.*

import catmoe.fallencrystal.translation.platform.Platform
import catmoe.fallencrystal.translation.platform.ProxyPlatform

class TranslationListener : EventListener {

    @Platform(ProxyPlatform.BUNGEE)
    @EventHandler(PlayerPostBrandEvent::class)
    @AsynchronousHandler
    fun handle(event: PlayerPostBrandEvent) { Listener.whenPostBrand(event) }

    @Platform(ProxyPlatform.BUNGEE)
    @EventHandler(PlayerJoinEvent::class, priority = HandlerPriority.LOWEST)
    @AsynchronousHandler
    fun handle(event: PlayerJoinEvent) { Listener.whenJoined(event) }

    @Platform(ProxyPlatform.BUNGEE)
    @EventHandler(PlayerLeaveEvent::class, priority = HandlerPriority.LOWEST)
    @AsynchronousHandler
    fun handle(event: PlayerLeaveEvent) { Listener.whenDisconnect(event) }

    @Platform(ProxyPlatform.BUNGEE)
    @EventHandler(PlayerChatEvent::class, priority = HandlerPriority.LOWEST)
    @AsynchronousHandler
    fun handle(event: PlayerChatEvent) { Listener.whenChat(event) }

    @Platform(ProxyPlatform.BUNGEE)
    @EventHandler(PlayerConnectServerEvent::class, HandlerPriority.LOWEST)
    @AsynchronousHandler
    fun handle(event: PlayerConnectServerEvent) { Listener.whenServerConnected(event) }

    @Platform(ProxyPlatform.BUNGEE)
    @EventHandler(PlayerSwitchServerEvent::class, HandlerPriority.LOWEST)
    @AsynchronousHandler
    fun handle(event: PlayerSwitchServerEvent) { Listener.whenSwitchServer(event) }
}