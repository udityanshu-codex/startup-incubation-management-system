@echo off
echo ======================================================================
echo    STARTUP INCUBATION SYSTEM — AUTOMATED END-TO-END TEST SUITE
echo ======================================================================
java -cp "lib/*;bin" test.EndToEndTests
pause
