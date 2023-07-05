package catmoe.fallencrystal.akaneconsole.util

import catmoe.fallencrystal.akaneconsole.config.Config
import com.typesafe.config.ConfigException

object Version  {
    fun getVersion(ver: Int): String{
        val config = Config.instance.config.getConfig("protocol")
        val unknown = config.getString("unknown").replace("%version", ver.toString())
        return try { config.getString(ver.toString()) ?: unknown } catch (_: ConfigException) { unknown }
    }
}