package server;

import model.Transaction;

import javax.swing.*;
import java.awt.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Date;

public class Server {
    private static final double SIMPLEINTERESTRATE = 0.15;//NTS: CHECK THIS
    private static final double COMPOUNDINTERESTRATE = 0.2;
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream outStream = null;
    private ObjectInputStream inStream = null;
    private static Connection connection = null;
    private PreparedStatement stmt;

    //Default constructor
    public Server() {
        this.createConnection();
        this.waitForRequest();
    }

    private void createConnection() {
        try {
            serverSocket = new ServerSocket(8888);
            if (serverSocket.equals(null)) {
                System.err.println("ServerSocket could not be started");
            }
            System.out.println("Server is running....\t\tLocalTime: " + new Date());
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public static Connection getDatabaseConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/labtest2bank";
            connection = DriverManager.getConnection(url, "root", "");

            JOptionPane.showMessageDialog(null, "DB Connection Established",
                    "CONNECTION STATUS", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to database\n" + e,
                    "Connection Failure", JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    public void saveTransaction(Transaction transaction) {
        try {
            String sql = "INSERT INTO transactions (account_number, transaction_type, " +
                    "transaction_amount, transaction_time) VALUES (?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, transaction.getAccountNumber());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getTransactionAmount());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Waiting for request from client
    private void waitForRequest() {

        getDatabaseConnection();
        System.out.println("Server started...waiting for clients");
        try {
            while (true) {
                socket = serverSocket.accept();
                // Start a new thread to handle the request
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            outStream = new ObjectOutputStream(socket.getOutputStream());
                            inStream = new ObjectInputStream(socket.getInputStream());
                            // Read the action from the input stream
                            String action = "";
                            action = (String) inStream.readObject();
                            while (!action.equalsIgnoreCase("exit")) {
                                if (action.equalsIgnoreCase("Process Transaction")) {
                                    // Read the Transaction object from the input stream
                                    Transaction transaction = (Transaction) inStream.readObject();
                                    // Save the transaction to the database
                                    saveTransaction(transaction);
                                }
                                // Read the next action from the input stream
                                action = (String) inStream.readObject();
                            }
                        } catch (EOFException ex) {
                            System.out.println("Client has terminated connections with the server");
                            ex.printStackTrace();
                        } catch (IOException | ClassNotFoundException ex) {
                            ex.printStackTrace();
                        } finally {
                            if (outStream != null) {
                                try {
                                    outStream.close();
                                    if (inStream != null) inStream.close();
                                    if (socket != null) socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                                                    }
                    }
                });
                t.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //This method is used to process a deposit and update the users information
    public Transaction deposit(Transaction transaction) {
        try {
            String sql = "UPDATE accounts SET balance = balance + ?, lastTransType = ? WHERE accountNumber = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDouble(1, transaction.getTransactionAmount());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setString(3, transaction.getAccountNumber());
            stmt.executeUpdate();

            // Update the user's account balance
            double newBalance = transaction.getAccountBalance() + transaction.getTransactionAmount();
            transaction.setAccountBalance(newBalance);
            // Update the last transaction type
            transaction.setTransactionType(transaction.getTransactionType());
            // Update the last transaction amount
            transaction.setTransactionAmount(transaction.getTransactionAmount());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transaction;
    }

    public Transaction withdrawal(Transaction transaction) {
        try {
            String sql = "UPDATE accounts SET balance = balance - ?, lastTransType = ?, lastTransAmount = ? WHERE account_number = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setDouble(1, transaction.getTransactionAmount());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getTransactionAmount());
            stmt.setString(4, transaction.getAccountNumber());
            stmt.executeUpdate();

            // Update the balance in the Transaction object
            transaction.setAccountBalance(transaction.getAccountBalance() - transaction.getTransactionAmount());

            // Set the lastTransType and lastTransAmount in the Transaction object
            transaction.setTransactionType(transaction.getTransactionType());
            transaction.setTransactionAmount(transaction.getTransactionAmount());

            // Return the updated Transaction object
            return transaction;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Transaction balanceCheck(Transaction transaction) {
        try {
            String sql = "SELECT balance, acct_type FROM accounts WHERE account_number = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, transaction.getAccountNumber());
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                double balance = result.getDouble("balance");
                String acctType = result.getString("acct_type");
                double interest = 0.0;
                if (acctType.equalsIgnoreCase("Saving") || acctType.equalsIgnoreCase("Checking")) {
                    interest = (balance * SIMPLEINTERESTRATE);
                } else if (acctType.equalsIgnoreCase("CD")) {
                    interest = (balance * (Math.pow(1 + (COMPOUNDINTERESTRATE), 1) * 1)) - balance;
                }
                String updateSql = "UPDATE accounts SET interest = ?, lastTransType = ? WHERE account_number = ?";
                stmt = connection.prepareStatement(updateSql);
                stmt.setDouble(1, interest);
                stmt.setString(2, transaction.getTransactionType());
                stmt.setString(3, transaction.getAccountNumber());
                stmt.executeUpdate();
                transaction.setAccountBalance(balance);
                return transaction;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
