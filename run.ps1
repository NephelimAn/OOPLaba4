param([switch]$Test)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk = $env:JAVA_HOME
if (-not $jdk -or -not (Test-Path (Join-Path $jdk 'bin/javac.exe'))) {
    $installed = Get-ChildItem "$env:USERPROFILE/.jdks" -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin/javac.exe') } |
        Sort-Object Name
    $preferred = $installed | Where-Object Name -Like '*21*' | Select-Object -First 1
    if (-not $preferred) { $preferred = $installed | Select-Object -First 1 }
    if ($preferred) { $jdk = $preferred.FullName }
}
if (-not $jdk) { throw 'Установите JDK 21 и задайте JAVA_HOME.' }
$compiler = Join-Path $jdk 'bin/javac.exe'
$java = Join-Path $jdk 'bin/java.exe'
New-Item -ItemType Directory -Force out | Out-Null
$sources = @(Get-ChildItem src -Filter '*.java' | ForEach-Object FullName)
if ($Test) { $sources += @(Get-ChildItem test -Filter '*.java' | ForEach-Object FullName) }
& $compiler -encoding UTF-8 -d out @sources
if ($LASTEXITCODE -ne 0) { throw 'Ошибка компиляции' }
if ($Test) { & $java '-Djava.awt.headless=true' -cp out ColonyTest }
else { & $java -cp out Main }
if ($LASTEXITCODE -ne 0) { throw 'Программа завершилась с ошибкой' }
