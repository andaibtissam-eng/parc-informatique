@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "BUNDLED_MVN=%SCRIPT_DIR%..\tools\apache-maven-3.9.9\bin\mvn.cmd"

if exist "%BUNDLED_MVN%" (
  call "%BUNDLED_MVN%" %*
  exit /b %ERRORLEVEL%
)

where mvn >nul 2>&1
if %ERRORLEVEL%==0 (
  mvn %*
  exit /b %ERRORLEVEL%
)

where docker >nul 2>&1
if %ERRORLEVEL%==0 (
  docker run --rm -v "%cd%:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-17 mvn %*
  exit /b %ERRORLEVEL%
)

echo Maven n'est pas installe et Docker n'est pas disponible.
echo Installez Maven 3.9+ ou Docker Desktop pour executer les commandes de build.
exit /b 1
