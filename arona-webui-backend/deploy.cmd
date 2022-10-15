@echo off

::你的MCL绝对路径
set MCL_PATH="C:\Users\Haythem Kenway\Desktop\mirai-console"
::调试端口，默认5005
set DEBUG_PORT=5005
set JAR_NAME=%1

copy .\build\mirai\%JAR_NAME% %MCL_PATH%\plugins
cd %MCL_PATH%
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT% -jar mcl.jar %*
exit 0