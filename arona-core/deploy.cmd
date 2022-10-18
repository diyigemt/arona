@echo off

set JAR_NAME=%1

::你的MCL根目录
set MCL_PATH="path/to/mcl"

copy .\build\mirai\%JAR_NAME% %MCL_PATH%\plugins
copy .\runMirai.cmd %MCL_PATH%

