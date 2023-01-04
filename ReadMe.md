# Simple Client-Server ATM
This is a simple ATM system implemented in Java using a client-server design and MySQL for storing user and transaction data.

### Features
The ATM allows users to perform the following actions:

Check their balance
Withdraw cash
Deposit cash

### Architecture
The ATM system consists of two main components: a server and a client.

The server is responsible for handling all ATM transactions and interacting with the MySQL database. It exposes a set of APIs for the client to use.

The client is a GUI interface that allows users to interact with the ATM by making requests to the server.

### MySQL Database
The ATM system uses a MySQL database to store user and transaction data. The database has the following tables:

users: stores user data such as username, password, and balance
transactions: stores transaction data such as transaction type, amount, and date

### Usage
To use the ATM, the server must be started first. Then, the client can be used to connect to the server and perform transactions.

### Running the Server
To start the server, run the ATMServer class. The server will start listening for client requests on the specified port.

### Running the Client
To start the client, run the ATMClient class. The client will prompt the user for the server address and port, as well as their username and password. Once the user is authenticated, they can perform transactions by following the prompts in the command-line interface.








