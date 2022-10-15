# Arona-WebUI-Backend

基于Ktor的HTTP服务端



## 介绍

该项目负责为Arona-WebUI-Frontend提供Web服务和将前端修改提交到Arona-core以应用设置。



## 项目搭建

### 注意

和Arona-core通信需要建立依赖关系，将Arona-core的插件jar包作为本地依赖，下载你希望调试的Arona-core版本（必须是1.0.11之后的）放进/plugins文件夹里，Gradle会加载该文件夹内所有jar作为本地依赖。



### 开发和调试

本项目使用Ktor2.1.2，与Mirai集成的Ktor1.6.7版本差距过大，以及需要插件之间的数据交换，故无法通过手动调用onLoad和onEnable等方法加载插件调试，需要执行Gradle task： 

```
buildPlugin
```

然后将打包好的jar包与Arona-core一起放进mcl中运行。当显示以下内容时：

```
2022-10-14 14:37:34 I/stdout: 14:37:34.478 [DefaultDispatcher-worker-4] INFO ktor.application - Responding at http://127.0.0.1:8080
```

代表Web服务已启动，打开浏览器访问本地8080端口即可进入。



调试需要使用远程调试：

1. 先执行以下命令启动mcl

```shell
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -jar mcl.jar
```

注：在根目录中有一个deploy.cmd脚本可以帮助你快速启动mcl

2. 配置一个远程JVM调试并开始调试