# IDENTITY SERVICE 🔐

A robust, production-ready authentication service featuring advanced security mechanisms, social login, and comprehensive monitoring.

## ✨ Features

### 🔒 Security Features
- **JWT-based Authentication** with access and refresh tokens
- **Two-Factor Authentication (2FA)** using TOTP (Time-based One-Time Password)
- **Social Login (OAuth2)** integration with major providers (Google, GitHub, Facebook)
- **Secure password policies** and encryption

### 📊 Monitoring & Health
- **Health Checks** with Spring Boot Actuator
- **Custom Metrics** with Micrometer and Prometheus
- **Application metrics** monitoring and dashboards

### 🗄️ Database & Migration
- **Database Migration** with Flyway for version-controlled schema changes
- **Data persistence** with proper rollback strategies

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 13+
- Redis (for token storage)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/charigardash/identity_service.git
