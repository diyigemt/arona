2022-10-02 v1.0.10

1. 攻略指令的别名覆写配置支持一对多
2. 修复N2H2防侠时间错误的问题
3. 修复配置文件中机器人运行的qq与实际不一样时仍会回应的问题
4. 攻略指令提供模糊搜索建议功能
5. 添加配置项arona-trainer.yml -> tipWhenNull 允许用户配置是否启用`/攻略`指令的模糊搜索提示，[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#other-name-config)
6. 添加配置项arona-trainer.yml -> fuzzySearchSource 允许用户配置`/攻略`指令的模糊搜索来源，[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#other-name-config)
7. 攻略指令的别名支持多对一配置，[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#other-name-config-multi)
8. 新增公告功能，可以接收作者的公告，[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#remote-announce)
9. 新增卡池自动更新功能，可以接收懒狗作者根据新池子内容自动更新本地抽卡数据库，[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#remote-pool-update)
10. 添加配置项arona.yml -> remoteCheckInterval 允许用户配置**远端**功能的开启与否，[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#remote)
11. 攻略图片下载失败时将会提供反馈信息
12. 修复某些情况下无法获取学生生日信息导致`/活动`指令失效的问题
13. 修复在服务器环境下部署时系统中没有中文字体导致`/活动`指令结果中文乱码的问题
14. 新增特殊配置文件`./data/net.diyigemt.arona/config/trainer_config.yml`允许用户在`mirai-console`运行时修改`/攻略`指令的别名覆写配置，具体看[这里](https://github.com/diyigemt/arona/blob/master/doc/using.md#other-name-config-2)
15. 添加指令`/抽卡 list`可查看最近两个池子的配置
16. 后端服务地址转移到国内的服务器上，优化`/攻略`指令第一次下载图片时的速度

版本迁移指南

1. 由于添加了模糊搜索依赖且该依赖在国内阿里云仓库中没有信息，所以需要执行[Arona安装 第6步](https://github.com/diyigemt/arona/blob/master/doc/install.md#install-6)

向`mirai-console`手动添加依赖

2. 认真看一遍上面的改动

老用户无须更新数据库文件，这是为新用户准备的

3. yaml文件对格式**十分敏感**，在保存之前**建议**使用在线网站**验证**文件内容格式的正确性