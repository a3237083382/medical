@echo off
set JAVA_HOME=D:\Program Files (x86)\jdk\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%
set MAVEN_OPTS=
cd /d D:\work\proj2\RuoYi-Vue
mvn clean package -DskipTests -s D:\work\proj2\tmp\my-settings.xml -Dmaven.repo.local=D:\work\proj2\tmp\.m2-repo
