#!/bin/bash
# Compile and run the Payment Gateway Swing GUI (Linux/macOS)

JAR="lib/mysql-connector-j-8.0.33.jar"
SRC="db/DBConnection.java gui/UserGUI.java gui/TransactionGUI.java gui/GatewayGUI.java gui/PaymentLogGUI.java gui/AuditTrailGUI.java gui/MainDashboard.java"

echo "==> Compiling..."
javac -cp ":$JAR" $SRC
if [ $? -ne 0 ]; then echo "Compilation failed."; exit 1; fi

echo "==> Running..."
java -cp ":$JAR" gui.MainDashboard
