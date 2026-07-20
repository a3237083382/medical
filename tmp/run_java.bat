start /B "" "D:\Program Files (x86)\jdk\jdk17\bin\java.exe" ^
  -Xms256m -Xmx1024m ^
  -jar "D:\work\proj2\RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar" ^
  --server.port=8088 ^
  1>"D:\work\proj2\backend.log" ^
  2>"D:\work\proj2\backend.err"
