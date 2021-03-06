# money-transfer-api

##  Task
Design and implement a RESTful API (including data model and the backing implementation) for money transfers between accounts.

##  Technologies used

+ Java 8
+ Maven
+ Vert X HTTP server
+ Logback (for logging)
+ Lombok
+ JUnit 5 (Integration testing the api)

##  Build and Run

 To build the jar file - `mvn clean install`

 To run the application as a standalone app -  `java -jar target/money-transfer-api-fat-executable.jar `
 
 ##  API endpoints
 
 ### Funds transfer endpoint 
   + Endpoint : http://localhost:6090/api/transactions
   + Request Type : POST
   + sample request : 
```javascript
  {
       {
        "accountNumber": 90909090,
        "sortCode": 808080
       },
	{
        "accountNumber": 19191919,
        "sortCode": 808080
       },
	    transferAmount: 900
  }
```
### Get accounts endpoint (For admin/reports purposes)
   + Endpoint : http://localhost:6090/api/accounts
   + Request Type : GET
 
### Get transactions for an account
   + Endpoint : http://localhost:6090/api/accounts/{account_id}/transactions/
   + Request Type : GET
 
### Get accounts for a customer
  + Endpoint : http://localhost:6090/api/customer/{customer_id}/accounts
  + Request Type : GET

### Create account
  + Endpoint : http://localhost:6090/api/accounts
  + Request Type : POST
     + sample request : 
```javascript
 {
 "sortCode": 606060,
 "customer":{
	"firstName":"Sam",
	"lastName":"Fox",
	"emailAddress":"sam.fox@companyb.com",
	"dateOfBirth":"01-01-1980"
 }
}
```
 
 ## Caveats
 + The API is configured to run on the port 6090 and this can be changed and hence the api endpoints would be like http://localhost:6090/
 + No security configured because that is not part of the requirement

 
 
