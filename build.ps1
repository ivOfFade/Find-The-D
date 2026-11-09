$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force out, target | Out-Null
    $sources = Get-ChildItem src/main -Recurse -Filter '*.java'
    $lines = foreach ($file in $sources) { '"' + $file.FullName.Replace('\', '/') + '"' }
    [IO.File]::WriteAllLines((Join-Path $PSScriptRoot 'target/sources.txt'), $lines, [Text.UTF8Encoding]::new($false))
    & javac -encoding UTF-8 -cp 'lib/*' -d out '@target/sources.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed. Read the Java errors above.' }
    Write-Host 'Compiled src/main into out. No Maven, tests, or stubs are used.'
} finally {
    Pop-Location
}
