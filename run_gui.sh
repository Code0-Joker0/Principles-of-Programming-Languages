#!/bin/bash
# Works on Linux and macOS regardless of MySQL Connector/J version
cd "$(dirname "$0")"

JAR=$(ls lib/mysql-connector*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
    echo "ERROR: No MySQL connector JAR found in lib/"
    exit 1
fi

SRC="db/DBConnection.java \
     gui/Theme.java \
     TransactionPackage/TransactionNotFoundException.java \
     TransactionPackage/Transaction.java \
     TransactionPackage/TransactionManager.java \
     gui/UserGUI.java \
     gui/GatewayGUI.java \
     gui/TransactionGUI.java \
     gui/PaymentLogGUI.java \
     gui/AuditTrailGUI.java \
     gui/MainDashboard.java \
     Main.java"

echo "==> Using JAR: $JAR"
echo "==> Compiling..."
javac -cp ".:$JAR" $SRC
if [ $? -ne 0 ]; then echo "Compilation failed."; exit 1; fi

echo "==> Running..."
java -cp ".:$JAR" Main
