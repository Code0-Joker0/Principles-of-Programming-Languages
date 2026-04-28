@echo off
REM Works on Windows regardless of MySQL Connector/J version
cd /d "%~dp0"

set JAR=
for %%f in (lib\mysql-connector*.jar) do set JAR=%%f

if "%JAR%"=="" (
    echo ERROR: No MySQL connector JAR found in lib\
    pause
    exit /b 1
)

set SRC=db\DBConnection.java ^
 gui\Theme.java ^
 TransactionPackage\TransactionNotFoundException.java ^
 TransactionPackage\Transaction.java ^
 TransactionPackage\TransactionManager.java ^
 gui\UserGUI.java ^
 gui\GatewayGUI.java ^
 gui\TransactionGUI.java ^
 gui\PaymentLogGUI.java ^
 gui\AuditTrailGUI.java ^
 gui\MainDashboard.java ^
 Main.java

echo =^> Using JAR: %JAR%
echo =^> Compiling...
javac -cp ".;%JAR%" %SRC%
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)

echo =^> Running...
java -cp ".;%JAR%" Main
