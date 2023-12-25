# Download Project structure from start.spring.io
---
# Modify application.properties
---
# Create different packages
- Constants, DAO, JWT, POJO, Rest, Restimpl, Service, Serviceimpl, utils, Wrapper
---
# Make POJO classes
- Make User class
# Make User class for all packages
---
# Make the rest class for user
- Add respective method in service and serviceImpl class
- Write findByEmailId query in User class
- In the service class write logic for signup:
- - Validate Data
- - Convert Map to User object
- - Check if email is already registered if not signup or else throw exception
- - Add proper exception and response entity

# JWT
- Make JwtUtil class
- Add dependency
- Create secret key
- Add differnt methods to work with JWT
- - Creating and validating JWT, Extracting clamis and different fields from JWT

# Making Security configuration class
- Make a CustomerDetailsSerive class bean to inject in the configuration class
- - This class finds and returns the UserDetails of the Email passed
- Create a JwtFilter class and pass in the addFilterBefore
- - In the JWT filter class get the user details and verify the token (from the authorization header) and user details
- - Set SecurityContextHolder
- Add the login code

# Getting and Updating the user
- Create GET and POST mapping
- Implement those methods
- Add service logic and authorization logic

# Sending mail
- Add the Java Mail dependency
```
<groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
	<!-- <version>2.7.5</version> -->
</dependency>
```
- Add the mail properties
- Write a EmailUtils class to send the mail by declaring JavaMailSender bean

# Add change and forgot password APIs

# Create Category Interfaces and classes
- Create Category POJO class
- Create Category DAO Interface
- Create Category REST and Service Interfaces and Classes

# Create Product Interfaces and classes
- Create Product POJO class
- Create Category DAO Interface
- Create Rest and Service Interfaces and classes
- Implement all the apis and methods

# Create Bill Interface and classes
- Create rest, rest impl, service and service impl imterface and classes
- GenerateReport API
- - Write the logic in the Service Impl class
- - 

mysql -u root -p
123456789
use cafemanagement;