CREATE DATABASE IF NOT EXISTS payment_gateway_db;
USE payment_gateway_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(15),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gateways (
    gateway_id INT AUTO_INCREMENT PRIMARY KEY,
    gateway_name VARCHAR(50) UNIQUE NOT NULL,
    api_endpoint VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    gateway_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (gateway_id) REFERENCES gateways(gateway_id)
);

CREATE TABLE IF NOT EXISTS payment_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    log_message TEXT,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS audit_trail (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50),
    operation ENUM('INSERT','UPDATE','DELETE'),
    record_id INT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

INSERT IGNORE INTO gateways (gateway_name, api_endpoint) VALUES
('Stripe',   'https://api.stripe.com'),
('PayPal',   'https://api.paypal.com'),
('Razorpay', 'https://api.razorpay.com');
