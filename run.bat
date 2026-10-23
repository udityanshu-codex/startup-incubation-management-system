@echo off
echo ======================================================================
echo    STARTUP INCUBATION AND INNOVATION MANAGEMENT SYSTEM
echo    Course: Object Oriented Programming using Java (CCSE0355)
echo    Institute: NIET Greater Noida
echo ======================================================================
echo Compiling project...
javac -cp "lib/*;src" -d bin src\Main.java src\utils\*.java src\model\*.java src\dao\*.java src\service\*.java src\gui\*.java src\gui\founder\*.java src\gui\admin\*.java src\test\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Application...
java -cp "lib/*;bin" Main
pause
