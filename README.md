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

 To run the application as a standalone app -  `java -jar ".\target\money-transfer-api-1.0-SNAPSHOT.jar" `
 
 ##  API endpoints
 
 + Funds transfer endpoint 
    sample request : http://localhost:6090/api/transactions
    Request Type : POST
```javascript
  {
       {
        "accountNumber": 90909090,
        "sortCode": 808080,
        "balance": 1000
       },
	{
        "accountNumber": 19191919,
        "sortCode": 808080,
        "balance": 2000
       },
	    transferAmount: 900
  }
```
 
 
 ## Caveats
 + The API is configured to run on the port 6090 and this can be changed and hence the api endpoints would be like http://localhst:6090/
 + No security configured because that is not part of the requirement
 
