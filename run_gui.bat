@echo off
REM Compile and run the Payment Gateway Swing GUI (Windows)

set JAR=lib\mysql-connector-j-8.0.33.jar
set SRC=db\DBConnection.java gui\UserGUI.java gui\TransactionGUI.java gui\GatewayGUI.java gui\PaymentLogGUI.java gui\AuditTrailGUI.java gui\MainDashboard.java

echo ==> Compiling...
javac -cp ".;%JAR%" %SRC%
if %errorlevel% neq 0 ( echo Compilation failed. & exit /b 1 )

echo ==> Running...
java -cp ".;%JAR%" gui.MainDashboard
