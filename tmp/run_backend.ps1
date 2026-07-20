$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = 'cmd.exe'
$psi.Arguments = '/c ""D:\Program Files (x86)\jdk\jdk17\bin\java.exe" -jar D:\work\proj2\RuoYi-Vue\ruoyi-admin\target\ruoyi-admin.jar > D:\work\proj2\backend.log 2>&1"'
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true
$null = [System.Diagnostics.Process]::Start($psi)
