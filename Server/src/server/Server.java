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
    private static Connection connection = null;
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream outStream = null;
    private ObjectInputStream inStream = null;
    private Statement stmt;
    private ResultSet result;

    //Default constructor
    public Server() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException |
                 InstantiationException e) {
            e.printStackTrace();
        }
        this.createConnection();
        this.waitForRequest();
    }

    public static Connection getDatabaseConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/commercialatm";
            connection = DriverManager.getConnection(url, "root", "");

            JOptionPane.showMessageDialog(null, "DB Connection Established",
                    "CONNECTION STATUS", JOptionPane.INFORMATION_MESSAGE);
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Could not connect to database\n" + e,
                    "Connection Failure", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return connection;
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
                            Transaction trans = (Transaction) inStream.readObject();
                            while (!trans.getTransactionType().equalsIgnoreCase("exit")) {
                                if (trans.getTransactionType().equalsIgnoreCase("Deposit")) {
                                    Transaction transaction = deposit(trans);
                                    outStream.writeObject(transaction);
                                }
                                if (trans.getTransactionType().equalsIgnoreCase("Withdraw")) {
                                    Transaction transaction = withdrawal(trans);
                                    outStream.writeObject(transaction);
                                }
                                if (trans.getTransactionType().equalsIgnoreCase("Balance Check")) {
                                    Transaction transaction = balanceCheck(trans);
                                    outStream.writeObject(transaction);
                                }
                                // Read the next action from the input stream
                                trans = (Transaction) inStream.readObject();
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
            double newBalance = 0;
            String query = "SELECT * FROM commercialatm.accounts WHERE acctNum = " + transaction.getAccountNumber();
            Statement stmt = Server.getDatabaseConnection().createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result == null) {
                JOptionPane.showMessageDialog(null, "Transaction with listed account number could not be found",
                        "Transaction Status", JOptionPane.ERROR_MESSAGE);
                return null;
            } else if (result.next()) {
                newBalance = result.getDouble(5) + transaction.getTransactionAmount();
            }
            transaction.setAccountBalance(newBalance);
            //Update transaction in database
            String updateSql = "UPDATE commercialatm.accounts SET lastTransType = '"
                    + transaction.getTransactionType() + "', lastTransAmount ='" + transaction.getTransactionAmount()
                    + "', balance = '" + newBalance + "' WHERE acctNum = '" + transaction.getAccountNumber() + "'";
            stmt = Server.getDatabaseConnection().createStatement();
            if (stmt.executeUpdate(updateSql) == 1) {
                return transaction;
            } else {
                JOptionPane.showMessageDialog(null, "An error occurred while updating the database. Invalid Account!",
                        "Transaction Status", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Transaction withdrawal(Transaction transaction) {
        try {
            String query = "SELECT * FROM commercialatm.accounts WHERE acctNum = " + transaction.getAccountNumber();
            double newBalance = 0;
            stmt = getDatabaseConnection().createStatement();
            result = stmt.executeQuery(query);
            if (result.next()) {
                newBalance = result.getDouble(5) - transaction.getTransactionAmount();
                if (newBalance < 0) {
                    JOptionPane.showMessageDialog(null, "You cannot withdraw more than your balance", "Insufficient Funds",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            }
            transaction.setAccountBalance(newBalance);
            String updateSql = "UPDATE commercialatm.accounts SET lastTransType = '"
                    + transaction.getTransactionType() + "', lastTransAmount ='"
                    + transaction.getTransactionAmount() + "', balance = '" + newBalance + "' WHERE acctNum = '"
                    + transaction.getAccountNumber() + "'";
            stmt = getDatabaseConnection().createStatement();
            if (stmt.executeUpdate(updateSql) == 1) {
                // Return the updated Transaction object
                return transaction;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return transaction;
    }

    public Transaction balanceCheck(Transaction transaction) {
        try {
            String sql = "SELECT balance, acctType FROM commercialatm.accounts WHERE acctnum = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
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

            /*////////////////
            Transaction updatedTransaction = new Transaction();
            String query = "SELECT * FROM labtest2bank.labtest2accounts WHERE acctNum = " + transaction.getAccountNumber();
            try {
                stmt = LabTest2Server.getDatabaseConnection().createStatement();
                result = stmt.executeQuery(query);
                if (result.next()) {
                    transaction.setTransactionType(result.getString(3));
                    transaction.setTransactionAmount(4);
                    transaction.setAccountBalance(result.getDouble(5));
                    updatedTransaction = transaction;
                }
            */////////////////
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}
