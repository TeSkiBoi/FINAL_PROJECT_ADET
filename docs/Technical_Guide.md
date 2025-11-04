# Technical Documentation

## Architecture Overview

### 1. Database Layer (db package)
```java
DbConnection.java
- Manages database connectivity
- Connection pooling
- MySQL connection parameters
- Error handling for database operations
```

### 2. User Interface Layer (ui package)
```java
Login.java
- User authentication
- Input validation
- Password hashing (planned)
- Session initialization

Dashboard.java
- Main application window
- Navigation management
- Statistics display
- Real-time updates

ProductPanel.java
- Product CRUD operations
- Search functionality
- Category management
- Input validation
- Data grid display

SupplierPanel.java
- Supplier CRUD operations
- Status management
- Contact information handling
- Search functionality
```

## Database Schema Details

### Users Table
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user',
    status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Products Table
```sql
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00
);
```

### Suppliers Table
```sql
CREATE TABLE suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    address TEXT,
    status VARCHAR(20) DEFAULT 'Active'
);
```

## Key Features Implementation

### 1. Authentication System
- Password encryption (planned enhancement)
- Session timeout handling
- Failed login attempt tracking
- Password reset functionality (planned)

### 2. Dashboard Statistics
- Real-time data updates
- Category-wise product counting
- Total inventory value calculation
- Supplier status monitoring

### 3. Product Management
- Barcode integration (planned)
- Image attachment support (planned)
- Stock level alerts
- Price history tracking

### 4. Data Validation Rules
- Product name: Required, max 100 chars
- Price: Positive decimal number
- Quantity: Non-negative integer
- Category: Selected from predefined list

## Error Handling
- Database connectivity errors
- Input validation errors
- Duplicate entry handling
- Transaction rollback support

## Performance Considerations
- Connection pooling
- Query optimization
- UI response time
- Memory management

## Security Measures
- SQL injection prevention
- Input sanitization
- Session management
- Access control

## Future Enhancements
1. User roles and permissions
2. Report generation
3. Barcode scanning
4. Email notifications
5. Data export/import
6. Audit logging