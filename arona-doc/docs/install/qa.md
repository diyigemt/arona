# 疑难问题

## 8.9.63协议code=45

2023-10-10更新

目前即使是`8.9.63`版本也出现了大规模拉闸的现象(code=45)，需要升级更高的版本协议，但是`fix-protocol-version`仓库下只有最高`8.9.63`协议

如果需要更高版本的协议，可以去这个仓库下载[MrXiaoM/qsign](https://github.com/MrXiaoM/qsign/tree/mirai/txlib)

在`txlib`文件夹下以版本命名的文件夹中的`android_phone.json`和`android_pad.json`即是对应版本的协议

::: warning

虽然升级协议版本能够解决问题，但是协议版本越高检测越严格，建议一个个往上试

:::

以`8.9.68`协议版本为例

1. 下载其中的`android_phone.json`并放到`mcl`根目录替换原先的`8.9.63`协议版本
2. 修改`KFCFactory.json`，将`8.9.63`改为`8.9.68`
3. 修改`unidbg-fetch-qsign-xxx`的启动命令，将`bin/unidbg-fetch-qsign --basePath=txlib/8.9.63`改为`bin/unidbg-fetch-qsign --basePath=txlib/8.9.68`
4. 修改`unidbg-fetch-qsign/txlib/8.9.68`中的`config.json`，将其中的`server`和`auto_register`内容改为和`8.9.63`中的一致，如果你有修改其他地方，也可以改成一样的，但是**注意**不要修改`protocol`相关的内容
5. 启动`unidbg-fetch-qsign`，然后启动`mcl`
6. 没了
