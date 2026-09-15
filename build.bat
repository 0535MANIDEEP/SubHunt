@echo off
set "JAVA_HOME=C:\jbr"
set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED
    exit /b 1
)
echo.
echo BUILD SUCCESSFUL - APK at app\build\outputs\apk\debug\app-debug.apk
