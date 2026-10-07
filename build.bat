@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE_DIR=C:\Users\maksi\tools\gradle-9.7.0"

cd /d "%~dp0"

echo [PaniniVision] Building with Java 25 + Gradle 9.7...
"%GRADLE_DIR%\bin\gradle.bat" build 2>&1

echo.
echo [PaniniVision] Build done. JAR at: build\libs\
echo.
pause
