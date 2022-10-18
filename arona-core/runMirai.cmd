@echo off

::调试端口，默认5005
set DEBUG_PORT=5005

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=%DEBUG_PORT% -jar mcl.jar