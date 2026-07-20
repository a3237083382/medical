@echo off
set JAVA_HOME=D:\Program Files (x86)\jdk\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%
start /B java -jar D:\work\proj2\RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar > D:\work\proj2\backend.log 2>&1
