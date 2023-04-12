# 安装mirai-console

::: warning
arona是依赖于mirai-console的插件，在安装arona之前，必须安装mirai-console
:::

## 安装mcl

方便起见, 使用[mcl-installer](https://github.com/iTXTech/mcl-installer)来安装mcl，它的下载地址可以在[这里找到](https://github.com/iTXTech/mcl-installer/releases)，目前最新的版本是[v1.0.7](https://github.com/iTXTech/mcl-installer/releases/tag/v1.0.7)

当然如果你想自己来也没关系，这里是mcl的[下载地址](https://github.com/iTXTech/mirai-console-loader/releases)，目前最新的版本是[v2.1.2](https://github.com/iTXTech/mirai-console-loader/releases/tag/v2.1.2)

在Windows上部署可以下载[mcl-installer-1.0.7-windows-amd64.exe](https://github.com/iTXTech/mcl-installer/releases/download/v1.0.7/mcl-installer-1.0.7-windows-amd64.exe)

在大部分云服务器上的Linux部署可以下载[mcl-installer-1.0.7-linux-amd64](https://github.com/iTXTech/mcl-installer/releases/download/v1.0.7/mcl-installer-1.0.7-linux-amd64)

其他环境我觉得应该不用我多说，你自己就知道

在你想装mcl的地方新建目录，把刚才下载好的installer放到目录里

在目录下打开控制台(在Windows环境下应该是`shift+右键`, 在Linux环境下, emm 应该不用开吧)

::: code-group

```powershell Windows
./mcl-installer-1.0.7-windows-amd64.exe
```

```bash Linux
./mcl-installer-1.0.7-linux-amd64
```

:::

接下来跟着引导安装即可

如果你看不懂它在干什么, 或许你可以查看[帮助界面](../other/help#mcl-installer)

安装完成后在控制台继续运行

```shell
./mcl
```

让mcl进行初始化，直到看到下面这个界面代表初始化成功

```shell
2023-04-10 17:07:49 I/plugin: Successfully loaded plugin MCL Addon v2.1.1
2023-04-10 17:07:49 I/main: Prepared built-in commands: autoLogin, help, login, logout, permission, status, stop
2023-04-10 17:07:49 I/MCL Addon: iTXTech MCL Version: 2.1.2-61c8bd8
2023-04-10 17:07:50 W/MCL Addon: iTXTech Soyuz 未安装，Soyuz MCL Handler 特性已禁用
2023-04-10 17:07:50 I/main: 1 plugin(s) enabled.
2023-04-10 17:07:50 I/main: mirai-console started successfully.
>
```

重点是`mirai-console started successfully.`这句话，并且在下一行出现输入光标`>`

输入`stop`并回车停止mcl，至此`mirai-console`的安装已经全部完成

