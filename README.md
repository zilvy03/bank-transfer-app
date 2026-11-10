# Bank Transfer System (Assignment 01)

Spring Boot + Thymeleaf + Spring Data JPA + MySQL + Lombok.

Demonstrates:
* `@Transactional` - a money transfer between two customers either fully succeeds or is fully rolled back
* `@Query` - JPQL, native SQL and UPDATE queries written in the repository interfaces

## Run
1. Start MySQL (XAMPP -> MySQL -> Start). The database `bank_db` is created automatically.
2. `./mvnw spring-boot:run`  (Windows: `mvnw.cmd spring-boot:run`)
3. Open http://localhost:8080  (3 sample customers are created on first start)

If your MySQL root user has a password, copy `secrets.properties.example` to `secrets.properties` and edit it.
"# Assignment_Grading" 
"# Bank_Tranfer_App" 


## Task 10: Transaction Rollback Experiment

### With @Transactional
- Simulated failure after debit.
- The transaction was rolled back.
- Total bank balance remained LKR 85,000.

### Without @Transactional
- Temporarily disabled transaction management on the transfer service.
- Simulated failure after debiting LKR 1,000.
- Total bank balance decreased from LKR 85,000 to LKR 84,000.
- The debit was not rolled back.

### Conclusion
@Transactional ensures that a money transfer is atomic.
If an error occurs during the transfer, all changes are rolled back.

After the experiment, the original @Transactional annotations were restored.
  