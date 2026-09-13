@echo off
chcp 65001 >nul
title 天天校园 - 安装 APK 到 WSA
setlocal

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
set "APKDIR=%~dp0输出APK"

echo ================================================
echo   把 APK 安装到 WSA（Windows 安卓子系统）
echo ================================================
echo.

if not exist "%ADB%" (
    echo [X] 找不到 adb：
    echo     %ADB%
    goto :end
)

if not exist "%APKDIR%" (
    echo [X] 找不到 APK 目录：
    echo     %APKDIR%
    echo     请先把下载的 APK 放进这个文件夹。
    goto :end
)

echo [1/3] 连接 WSA ...
"%ADB%" connect 127.0.0.1:58526

echo.
echo [2/3] 建立端口映射 ...
"%ADB%" reverse tcp:3000 tcp:3000

echo.
echo [3/3] 安装 APK ...
set FOUND=0
for %%f in ("%APKDIR%\*.apk") do (
    set FOUND=1
    echo.
    echo   正在安装：%%~nxf
    "%ADB%" install -r "%%f"
)

if "%FOUND%"=="0" (
    echo.
    echo   [X] 目录里没有 .apk 文件
    echo       请把 app-child-debug.apk / app-parent-debug.apk 放进：
    echo       %APKDIR%
)

echo.
echo ------------------------------------------------
echo  完成
echo.
echo  若提示 unauthorized，请在 WSA 窗口里点
echo  「允许 USB 调试」（可勾选一律允许），然后重跑一次。
echo ------------------------------------------------

:end
echo.
pause
endlocal
