$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath (Join-Path $PSScriptRoot '..')
try {
    & .\gradlew.bat testDebugUnitTest lintDebug assembleDebug
    if ($LASTEXITCODE -ne 0) { throw 'Build or verification failed.' }
    New-Item -ItemType Directory -Force dist | Out-Null
    $appBuild = Get-Content app/build.gradle.kts -Raw
    if ($appBuild -notmatch 'versionName = "([^"]+)"') { throw 'Missing version.' }
    $previewName = "Virtue-Tracker-$($Matches[1])-preview.apk"
    Copy-Item -LiteralPath app/build/outputs/apk/debug/app-debug.apk -Destination (Join-Path dist $previewName) -Force
    $previewDigest = (Get-FileHash -LiteralPath (Join-Path dist $previewName) -Algorithm SHA256).Hash.ToLowerInvariant()
    Set-Content -LiteralPath dist/SHA256SUMS.txt -Value "$previewDigest  $previewName"
} finally { Pop-Location }
