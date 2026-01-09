$ErrorActionPreference = "Stop"

$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"
$zipPath = "maven.zip"
$installDir = "maven_install"

Write-Host "Downloading Maven..."
Invoke-WebRequest -Uri $mavenUrl -OutFile $zipPath

Write-Host "Extracting Maven..."
if (Test-Path $installDir) { Remove-Item -Recurse -Force $installDir }
Expand-Archive -Path $zipPath -DestinationPath $installDir -Force

$mavenHome = Get-ChildItem -Path $installDir | Select-Object -First 1
$mvnBin = Join-Path $mavenHome.FullName "bin\mvn.cmd"

Write-Host "Maven installed at: $mvnBin"
Write-Host "Building Project..."

& $mvnBin clean package

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build Successful!"
} else {
    Write-Host "Build Failed!"
    exit 1
}
