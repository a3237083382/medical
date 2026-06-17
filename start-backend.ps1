$env:JAVA_HOME="D:/Program Files (x86)/jdk/jdk17"
$env:Path="$env:JAVA_HOME/bin;$env:Path"
java -jar D:\work\proj2\RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar 2>&1 | Out-File D:\work\proj2\backend.log -Encoding utf8 -Append
