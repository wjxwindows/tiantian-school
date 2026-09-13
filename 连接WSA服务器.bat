@echo off
chcp 65001 >nul
title 天天校园 - WSA 服务器连接
setlocal

set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

echo ================================================
echo   天天校园  ·  WSA(Windows 安卓子系统) 连接脚本
echo ================================================
echo.

if not exist "%ADB%" (
    echo [X] 找不到 adb：
    echo     %ADB%
    echo     请确认已安装 Android SDK Platform-Tools。
    goto :end
)

echo [1/3] 连接 WSA ...
"%ADB%" connect 127.0.0.1:58526

echo.
echo [2/3] 把 WSA 的 3000 端口映射到本机 3000 ...
"%ADB%" reverse tcp:3000 tcp:3000

echo.
echo [3/3] 当前端口映射：
"%ADB%" reverse --list

echo.
echo ------------------------------------------------
echo  完成。在 WSA 里的「天天校园」中把 API 地址填成：
echo      http://127.0.0.1:3000/
echo  App 启动时也会自动探测，通常无需手动填写。
echo.
echo  若上面提示 unauthorized，请在 WSA 窗口里
echo  点击「允许 USB 调试」（可勾选一律允许）。
echo ------------------------------------------------

:end
echo.
pause
endlocal
