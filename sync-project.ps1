$ErrorActionPreference='Stop'
$stageRoot=$PSScriptRoot
$projectRoot='C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie3'
$taskRoot='C:\Users\49157\Documents\Codex\2026-10-29\new-chat'
$baseline=Get-Content -LiteralPath (Join-Path $stageRoot 'baseline.json') -Raw | ConvertFrom-Json -AsHashtable
foreach($relative in $baseline.Keys) {
    $current=Join-Path $projectRoot $relative
    if(-not(Test-Path -LiteralPath $current)-or (Get-FileHash -LiteralPath $current -Algorithm SHA256).Hash -ne $baseline[$relative]) {
        throw "Projekt wurde parallel geändert; kein Überschreiben: $relative"
    }
}
$reportRoot=Join-Path $taskRoot 'work\warraq-3.1-2026-10-30'
if(Test-Path -LiteralPath (Join-Path $reportRoot 'source-before')) {throw 'Vorherige Sicherung existiert bereits; nicht überschreiben.'}
$backup=Join-Path $reportRoot 'source-before'
New-Item -ItemType Directory -Path $backup -Force | Out-Null
foreach($relative in $baseline.Keys) {
    $target=Join-Path $backup $relative
    New-Item -ItemType Directory -Path ([IO.Path]::GetDirectoryName($target)) -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $projectRoot $relative) -Destination $target
}
$staged=Join-Path $stageRoot 'project'
$files=rg --files --hidden -g '!**/build/**' -g '!**/.gradle/**' -g '!**/.kotlin/**' $staged
$active=@{}
foreach($file in $files) {
    $relative=[IO.Path]::GetRelativePath($staged,$file)
    $active[$relative]=$true
    $target=Join-Path $projectRoot $relative
    if(-not(Test-Path -LiteralPath $target)-or (Get-FileHash -LiteralPath $file).Hash -ne (Get-FileHash -LiteralPath $target).Hash) {
        New-Item -ItemType Directory -Path ([IO.Path]::GetDirectoryName($target)) -Force | Out-Null
        Copy-Item -LiteralPath $file -Destination $target -Force
        Write-Output "Aktualisiert: $relative"
    }
}
foreach($relative in $baseline.Keys) {
    if(-not $active.ContainsKey($relative)) {
        $target=[IO.Path]::GetFullPath((Join-Path $projectRoot $relative))
        if(-not $target.StartsWith($projectRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) {throw 'Ungültiges Ziel'}
        Remove-Item -LiteralPath $target
        Write-Output "Durch neue Quelldatei ersetzt; Original gesichert: $relative"
    }
}
Copy-Item -LiteralPath (Join-Path $stageRoot 'Warraq-3.1.apk') -Destination (Join-Path $taskRoot 'outputs\Warraq-3.1.apk')
Copy-Item -LiteralPath (Join-Path $stageRoot 'evidence') -Destination $reportRoot -Recurse
Copy-Item -LiteralPath (Join-Path $stageRoot 'build.log') -Destination (Join-Path $reportRoot 'build.log')
Copy-Item -LiteralPath (Join-Path $stageRoot 'baseline.json') -Destination (Join-Path $reportRoot 'baseline.json')
Write-Output $reportRoot
