param([ValidateSet('Debug','Release','Tests')][string]$Target='Debug')
$ErrorActionPreference='Stop'
$project=Join-Path (Split-Path $PSScriptRoot -Parent) 'Code\Android'
$localBuild=Join-Path $env:LOCALAPPDATA 'Waraq\Build'
New-Item -ItemType Directory -Path $localBuild -Force | Out-Null
$env:WARAQ_BUILD_ROOT=Join-Path $localBuild 'outputs'
if(-not $env:JAVA_HOME) {
    $candidate='C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\Java\jdk-17.0.20.1+1'
    if(Test-Path -LiteralPath (Join-Path $candidate 'bin\java.exe')) {$env:JAVA_HOME=$candidate}
}
$init=Join-Path $localBuild 'build-location.gradle'
# Ohne BOM: Gradle/Groovy lehnt ein UTF-8-BOM am Dateianfang ab (PowerShell 5.1 schreibt sonst eins).
[IO.File]::WriteAllText($init,@'
allprojects {
    layout.buildDirectory.set(new File(System.getenv('WARAQ_BUILD_ROOT'), project.name))
}
'@,(New-Object Text.UTF8Encoding $false))
$tasks=@(switch($Target) {'Debug'{'assembleDebug'} 'Release'{'assembleRelease'} 'Tests'{'assembleDebug','assembleDebugAndroidTest'}})
# Gradle schreibt Fortschritt nach stderr; mit 'Stop' würde PowerShell 5.1 daran abbrechen.
$ErrorActionPreference='Continue'
& (Join-Path $project 'gradlew.bat') -p $project --project-cache-dir (Join-Path $localBuild 'project-cache') --init-script $init --console=plain @tasks 2>&1 | ForEach-Object {"$_"}
if($LASTEXITCODE-ne 0){throw 'Build fehlgeschlagen. Keine Installation ausgeführt.'}
Write-Output ('Build-Ausgaben: '+$env:WARAQ_BUILD_ROOT)
Write-Output 'Keine Installation ausgeführt. Release muss separat mit dem bestehenden externen Schlüssel signiert werden; Debug-/Test-APKs vor Installation ebenfalls auf Signaturkompatibilität prüfen.'
