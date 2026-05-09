param(
    [int]$Port = 8081
)

$ErrorActionPreference = "Stop"

$project = Resolve-Path (Join-Path $PSScriptRoot "..")
$target = Join-Path $project "target\classes"
$m2 = Join-Path $env:USERPROFILE ".m2\repository"

$existing = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Iris Assistant is already running on http://localhost:$Port"
    exit 0
}

if (-not (Test-Path $m2)) {
    throw "Local Maven repository not found at $m2. Install Maven or run dependency resolution first."
}

New-Item -ItemType Directory -Force -Path $target | Out-Null

$sources = Get-ChildItem -Path (Join-Path $project "src\main\java") -Recurse -Filter *.java |
        ForEach-Object { $_.FullName }
$jars = Get-ChildItem -Path $m2 -Recurse -Filter *.jar -ErrorAction SilentlyContinue |
        ForEach-Object { $_.FullName }

$classpath = ($jars -join ";")
$argFile = Join-Path $project "target\javac.args"
$lines = @("-encoding", "UTF-8", "-cp", $classpath, "-d", $target) + $sources
Set-Content -Path $argFile -Value $lines

javac "@$argFile"
Copy-Item -Path (Join-Path $project "src\main\resources\*") -Destination $target -Recurse -Force

$runtimeClasspath = @($target) + $jars -join ";"
java -cp $runtimeClasspath com.iris.assistant.IrisAssistantApplication "--server.port=$Port"
