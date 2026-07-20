$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = 'cmd.exe'
$psi.Arguments = '/c "cd /d D:\work\proj2\RuoYi-Vue3 && D:\nodejs\npx.cmd vite --port 80 --host > D:\work\proj2\frontend.log 2>&1"'
$psi.UseShellExecute = $false
$psi.CreateNoWindow = $true
$null = [System.Diagnostics.Process]::Start($psi)
