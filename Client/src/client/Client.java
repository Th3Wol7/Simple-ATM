package client;

import model.Transaction;

import java.io.*;
import java.net.Socket;

public class Client {
    private Socket connectionSocket;
    private ObjectOutputStream objOs;
    private ObjectInputStream objIs;

    // Default Constructor
    public Client() {
        this.createConnection();
        this.configureStreams();
    }

    private void createConnection() {
        try {
            // Creating a socket to connect to the server
            connectionSocket = new Socket("127.0.0.1", 8888);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void configureStreams() {
        try {
            // Creates an input stream to recieve data from the server
            objIs = new ObjectInputStream(connectionSocket.getInputStream());
            // Creates an output stream to send data to the server
            objOs = new ObjectOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            objOs.close();
            objIs.close();
            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAction(String action) {
        try {
            objOs.writeObject(action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTransaction(Transaction transaction) {
        try {
            objOs.writeObject(transaction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Transaction receiveResponse() {
        Transaction transaction = null;
        try {
            transaction = (Transaction) objIs.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return transaction;
    }

}

