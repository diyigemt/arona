# 调试

由于Arona-Core中使用了基于Ktor2.1.2的Web服务，与mirai-core-api官方使用的ktor-client-core-jvm1.6.7冲突。所以此项目无法运行在单元测试环境中，只能打包放进mcl中以远程调试的方式进行。



## 调试环境搭建

1. 在arona-core根目录中找到deploy.cmd，修改以下内容然后保存：

   ```shell
   ::你的MCL根目录
   set MCL_PATH="path\to\mcl"
   ```

2. 在IDEA中新建一个远程调试配置(Remote JVM Debug)，调试模式选择Attach to remote JVM（默认），端口选择5005（默认）

3. 修改代码和下断点

4. 找到Gradle task: buildAndDeploy运行（此任务会编译mirai插件并将输出的jar包和启动脚本复制粘贴到mcl中）

5. 控制台进入mcl根目录

6. 运行runMirai.cmd，等待控制台输出以下内容：

   ```shell
   Listening for transport dt_socket at address: 5005
   ```

7. 启动IDEA配置好的远程调试

   

## 附加命令

Windows端适用

#### 查找占用5005端口的进程

```shell
netstat -ano|findstr "5005"
```

#### 杀掉PID进程

```shell
taskkill /T /F /PID 12345
```

