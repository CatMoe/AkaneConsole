package catmoe.fallencrystal.akaneconsole.config

import catmoe.fallencrystal.akaneconsole.logger.LogType
import catmoe.fallencrystal.translation.utils.config.CreateConfig

import java.io.File

class Config(file: File) {

    private val default = """
                # 可自定义占位符. 您可以创建无限行 并且支持MiniMessage
                # 但请注意 不一定所有终端都支持十六进制颜色 因此仍然建议使用传统颜色
                #
                # 自带占位符列表:
                # %version% - 玩家的版本号 在配置文件的底部可配置
                # %host% - 玩家从哪个域名或地址连接到服务器
                # %name% - 玩家的名称
                # %displayname% - 包含LuckPerms前后缀的名称 如果未安装LuckPerms或未设置 则什么也不返回
                # %address% - 玩家的IP地址
                # %server% - 玩家所处的服务器名称 如果他们没有连接到任何服务器 那就什么也不返回
                #
                # 如果占位符返回了自身的内容 那么说明占位符不支持此操作 (e.x Ping时写入%name%)
                # 如果以下占位符覆盖了原有占位符 将会优先使用这里的占位符 所以谨慎覆盖.
                placeholder {
                prefix="[AkaneConsole] "
                iHasAPlaceholder="<gradient:green:yellow>Best placeholder here</gradient>"
            }
            messages {
                # 如果消息为空则不发送
                ping="%prefix%%address% ping了一下服务器 (%version%)"
                join="%prefix%%displayname% [%address%] 从 %host% 连接到了服务器"
                join-server="%prefix%%displayname% 已于 %server% 服务器建立连接"
                switch-server="%prefix%%displayname% %from% -> %target%"
                leave-server="%prefix%%displayname% 主动与 %from% 服务器断开了连接"
                disconnect="%prefix%%displayname% 已从BungeeCord断开连接"
                kick="%prefix%%displayname% 因 %reason% 从 %from% 服务器断开连接"
                client-brand="%prefix%%displayname% 正在使用 %brand% 客户端"
                failed-verify="%prefix%<yellow>无法验证玩家. 因为访问Mojang会话API时出现了一些问题..."
                chat {
                    default="%prefix%[Chat] %displayname%<gray> : %message%"
                    proxy-command="%prefix%[ProxyCommand] %displayname%<gray> : %message%"
                    backend-command="%prefix%[BackendCommand] %displayname%<gray> : %message%"
                    cancelled="<red>[已取消事件]"
                    ignore-commands=[
                        "login",
                        "l",
                        "register",
                        "reg",
                        "premium",
                        "cracked",
                        "createpassword",
                        "changepassword",
                        "cp"
                    ]
                }
            }
            protocol {
                763="1.20.0/1"
                762="1.19.4"
                761="1.19.3"
                760="1.19.1/2"
                759="1.19"
                758="1.18.2"
                757="1.18.0/1"
                756="1.17.1"
                755="1.17"
                754="1.16.4/5"
                753="1.16.3"
                751="1.16.2"
                736="1.16.1"
                735="1.16"
                578="1.15.1/2"
                573="1.15"
                498="1.14.4"
                490="1.14.3"
                485="1.14.2"
                480="1.14.1"
                477="1.14"
                404="1.13.2"
                401="1.13.1"
                393="1.13"
                340="1.12.2"
                338="1.12.1"
                335="1.12"
                316="1.11.2"
                315="1.11"
                210="1.10.2"
                110="1.9.4"
                109="1.9.2/3"
                108="1.9.1"
                107="1.9"
                47="1.8.x"
                unknown="Unknown (%version%)"
            }
    """.trimIndent()

    private val util = CreateConfig(file)

    init { instance=this; util.setDefaultConfig(default); util.onLoad() }

    var config = util.getConfig()!!

    private var placeholder = loadPlaceholder()
    var message = loadMessage()

    private fun loadPlaceholder(): Map<String, String> {
        val placeholderConfig = config.getConfig("placeholder")
        val result = mutableMapOf<String, String>()
        placeholderConfig.root().keys.forEach { result["%$it%"]=placeholderConfig.getString(it) }
        return result
    }

    private fun loadMessage(): Map<LogType, String> {
        val config = config.getConfig("messages")
        val result = mutableMapOf<LogType, String>()
        for (it in LogType.values()) {
            var message = config.getString(it.path)
            placeholder.forEach { (placeholder, m) -> message=message.replace(placeholder, m) }
            result[it]=message
        }
        return result
    }

    companion object {
        lateinit var instance: Config
            private set
    }
}
