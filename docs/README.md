# Inventory Management System

## Overview
A Java Swing-based desktop application for managing inventory, products, and suppliers. The system provides a user-friendly interface for tracking products, managing suppliers, and monitoring inventory statistics.

## Features
- Secure user authentication system
- Dashboard with real-time statistics
- Product management with search functionality
- Supplier management with status tracking
- Category-based product organization
- Responsive user interface with modern design

## Technical Stack
- **Language:** Java
- **UI Framework:** Java Swing
- **Database:** MySQL
- **Architecture:** MVC Pattern
- **Build System:** Eclipse IDE

## System Requirements
- Java JDK 17 or higher
- MySQL Server 8.0 or higher
- Eclipse IDE 2021 or higher
- Minimum 4GB RAM
- Windows/Linux/macOS

## Installation
1. Clone the repository
2. Import the project into Eclipse
3. Set up MySQL database using `database/setup.sql`
4. Configure database connection in `src/db/DbConnection.java`
5. Run the application from `src/ui/Login.java`

## Default Credentials
- Username: admin
- Password: admin123

## Project Structure
```
InventoryManagement/
├── src/
│   ├── ui/           # User interface components
│   ├── db/           # Database connectivity
│   └── model/        # Data models
├── database/         # SQL scripts
└── docs/            # Documentation
```

## Key Components
1. **Login System**
   - Secure authentication
   - Password visibility toggle
   - Session management

2. **Dashboard**
   - Product statistics
   - Category-wise breakdown
   - Quick navigation menu
   - Logout functionality

3. **Product Management**
   - Add/Edit/Delete products
   - Category-based organization
   - Search functionality
   - Inventory tracking

4. **Supplier Management**
   - Supplier registration
   - Status tracking
   - Contact information
   - Active/Inactive status

## Database Schema
- users
- products
- suppliers
- categories

## Security Features
- Password protection
- Session management
- Input validation
- SQL injection prevention

## Contributors
- [Your Name]
- [Other Contributors]

## License
This project is licensed under the MIT License - see the LICENSE file for details