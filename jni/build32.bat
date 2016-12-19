@echo off
if not defined DevEnvDir ( call "C:\Apps\Visual Studio\VC\bin\vcvars32.bat" )
rm -rf target
mkdir target\x86
cd target\x86

echo.
echo Building...
cl ^
	/LD /Ox /GR- ^
	/I ..\.. /I ..\..\jni-headers /I ..\..\jni-headers\win32 ^
	..\..\com.esotericsoftware.clippy.tobii.EyeX.c ^
	/Feclippy.dll "..\..\eyex\x86\Tobii.EyeX.Client.lib"
if %errorlevel% neq 0 (
	cd ..\..
	exit /b %errorlevel%
)

echo.
echo Updating natives JAR...
cd ..
jar uf ..\..\libs\clippy-natives.jar x86\clippy.dll
cd ..\eyex
jar uf ..\..\libs\clippy-natives.jar x86\Tobii.EyeX.Client.dll
cd ..
