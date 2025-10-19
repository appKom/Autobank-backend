# Autobank - Receipt Management System

Autobank is a Kotlin Spring Boot application designed for managing financial receipts and economic requests for committees at NTNU Online. The system provides a comprehensive solution for receipt submission, review processes, and administrative functions with secure OAuth2/JWT authentication.

## 🚀 Quick Start

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/appKom/gnocchitony.git
   cd gnocchitony
   ```

2. **Configure environment variables**

3. **Run the application**
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080`

## 📋 Features

- **Receipt Management**: Create, view, and manage financial receipts
- **Admin Review System**: Approve or deny receipts with comments
- **Committee Integration**: Associate receipts with specific committees
- **File Attachments**: Upload and manage receipt attachments via Azure Blob Storage
- **Economic Requests**: Submit and manage economic requests (partial implementation)
- **Role-based Access Control**: Admin and regular user permissions
- **OAuth2 Authentication**: Secure authentication via Auth0

## 🏗️ Architecture

- **Backend**: Kotlin + Spring Boot
- **Database**: Microsoft SQL Server (Azure)
- **Authentication**: OAuth2/JWT via Auth0
- **File Storage**: Azure Blob Storage
- **Build Tool**: Gradle

## 📚 Documentation

Detailed documentation is available in the `/docs` folder:

- [API Routes](docs/api-routes.md) - Complete API endpoint documentation
- [Database Schema](docs/database-schema.md) - Database structure and relationships
- [Architecture](docs/architecture.md) - System architecture and design patterns
- [Setup & Deployment](docs/setup-deployment.md) - Detailed setup and deployment guide

## 🔧 Configuration

## 🔐 Authentication

All API endpoints require authentication via Bearer token:

```
Authorization: Bearer <access_token>
```

**Note**: In development mode (`environment=dev`), security is disabled for easier testing.

## 🛠️ Development

### Project Structure
```
src/main/kotlin/com/example/autobank/
├── controller/          # REST controllers
├── service/            # Business logic
├── repository/         # Data access layer
├── data/              # DTOs and data models
├── security/          # Security configuration
└── AutobankApplication.kt
```

### Key Endpoints

- `GET /api/auth/getuser` - Get current user info
- `POST /api/receipt/create` - Create new receipt
- `GET /api/receipt/getall` - List user receipts
- `GET /api/admin/receipt/all` - Admin: List all receipts
- `POST /api/admin/receipt/review` - Admin: Review receipt

For complete API documentation, see [docs/api-routes.md](docs/api-routes.md).

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is part of NTNU Online's application suite.

