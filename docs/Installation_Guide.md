# Installation and Setup Guide

## Prerequisites
1. Java Development Kit (JDK) 17 or higher
2. MySQL Server 8.0 or higher
3. Eclipse IDE 2021 or higher
4. MySQL Connector/J (JDBC driver)

## Step-by-Step Installation

### 1. Database Setup
1. Open MySQL Workbench or MySQL Command Line Client
2. Log in to MySQL with administrator privileges
3. Navigate to the `database` folder
4. Execute `setup.sql` script to:
   - Create the database
   - Set up required tables
   - Insert initial data
   - Create necessary indexes

### 2. Project Setup in Eclipse
1. File → Import → General → Existing Projects into Workspace
2. Select the `InventoryManagement` folder
3. Ensure all project files are checked
4. Click "Finish"

### 3. Configure Database Connection
1. Open `src/db/DbConnection.java`
2. Update the following parameters if needed:
   ```java
   String url = "jdbc:mysql://localhost:3306/inventorydb";
   String user = "root";
   String password = "your_password";
   ```

### 4. Add Required Libraries
1. Right-click project → Properties → Java Build Path
2. Add External JARs:
   - mysql-connector-java-8.0.x.jar
   - Any additional required libraries

### 5. Build and Run
1. Clean project: Project → Clean
2. Rebuild project: Project → Build
3. Run Login.java as Java Application

## Verification Steps

### 1. Database Connection
1. Launch application
2. Check console for "Database Connected Successfully!" message
3. Verify no connection errors

### 2. Login Test
1. Use default credentials:
   - Username: admin
   - Password: admin123
2. Verify successful login
3. Check dashboard loads properly

### 3. Feature Testing
1. Test Product Management:
   - Add new product
   - Search functionality
   - Update existing product
   - Delete product

2. Test Supplier Management:
   - Add new supplier
   - Search suppliers
   - Update supplier info
   - Verify status changes

### 4. Data Verification
1. Check dashboard statistics
2. Verify product counts
3. Confirm supplier listings
4. Test category breakdowns

## Troubleshooting

### Common Issues and Solutions

1. Database Connection Fails
   ```
   Solution:
   - Verify MySQL is running
   - Check connection credentials
   - Confirm port availability
   - Test network connectivity
   ```

2. Login Issues
   ```
   Solution:
   - Verify database users table
   - Check credentials case-sensitivity
   - Clear application cache
   ```

3. UI Display Problems
   ```
   Solution:
   - Update Java version
   - Check screen resolution
   - Verify system requirements
   ```

### System Requirements

1. Hardware
   - Processor: 2GHz or faster
   - RAM: 4GB minimum
   - Storage: 500MB free space

2. Software
   - Windows 10/11, macOS 10.14+, or Linux
   - Java Runtime Environment
   - MySQL Server
   - Internet connection for updates

## Maintenance

### Regular Tasks
1. Database backup
2. Log file cleanup
3. Index optimization
4. Cache clearing

### Security Updates
1. Change default passwords
2. Update MySQL credentials
3. Review access logs
4. Check for vulnerabilities

## Support Contact
For installation support:
- Technical Email: tech.support@system.com
- Emergency Contact: (XXX) XXX-XXXX
- Documentation: [Project Wiki]

## Version Information
- Application Version: 1.0.0
- Database Version: 1.0
- Last Updated: November 4, 2025
- Compatibility: JDK 17+, MySQL 8.0+