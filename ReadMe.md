# Simple Client-Server ATM
This is a simple ATM system implemented in Java using a client-server design and MySQL for storing user and transaction data.

### Features
The ATM allows users to perform the following actions:
- Check their balance
- Withdraw cash
- Deposit cash

### Architecture
The ATM system consists of two main components: a server and a client.

The server is responsible for handling all ATM transactions and interacting with the MySQL database. It exposes a set of APIs for the client to use.
The `Server` class creates a connection to the MySQL database and starts the server socket to listen for requests from clients. It contains the methods for:
Creating a connection to the MySQL database using JDBC. <br/>
Waiting for a request from a client and starts a new thread to handle the request. <br/>
Handling deposit and withdrawal requests from a client by updating the account balance in the database and returning the updated transaction object to the client.
Handling balance check requests from a client by retrieving the account balance from the database and returning the updated transaction object to the client.

The client is a graphical user interface that allows users to interact with the ATM by making requests to the server. The client is implemented using the `Client` class, which handles the communication with the server using `Socket` and `ObjectStream` objects. The `MainScreen` class is responsible for the layout and functionality of the client interface, including the confirmation and completion of transactions.

### Dependenicies
- mysql-connector-Java versions 8.0.30 was used in this project.
  - In Order for the server to work the librabry mentioned above must be added to the projects class path. 

### MySQL Database
The ATM system uses a MySQL database to store user and transaction data. The database has the following table:
- accounts: stores user data such as account number, last transaction type, balance, account type, etc.

### Usage
To use the ATM, the server must be started first. Then, the client can be used to connect to the server and perform transactions. The client can be launched by running the `main` class in the client module.

### Running the Server
To run the server, simply run the `ServerMain` class. The server will start and listen for requests from clients on port 8888.

### Running the Client
To start the client, run the `main` class. The client will prompt the user for their account number and display a list of available transaction types. The user can then enter the amount of the transaction and confirm or cancel the transaction. The client will display the results of the transaction and allow the user to perform additional transactions or exit.







