package view;

import client.Client;
import model.Transaction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class MainScreen extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JPanel mainPanel;
    private JLabel transAmountLabel;
    private JLabel accNumLabel;
    private JLabel transTypeLabel;
    private JTextArea displayArea;
    private JTextField accNumField;
    private JTextField transAmountField;
    private JComboBox<String> transactionType;
    private JButton confirmButton;
    private JButton finishButton;
    private JButton exitButton;

    public MainScreen() {
        initializeComponents();
        addComponentsToPanel();
        setWindowProperties();
        registerListeners();
    }

    public void initializeComponents() {
        new JFrame("My Awesome ATM");
        this.setLayout(null);

        String transaction[] = {"Deposit", "Withdrawl", "Balance Check"};
        mainPanel = new JPanel();
        mainPanel.setSize(600, 500);
        mainPanel.setLayout(null);
        accNumLabel = new JLabel("Account Number:");
        accNumLabel.setBounds(20, 40, 150, 30);

        accNumField = new JTextField(7);
        accNumField.setBounds(200, 40, 150, 30);

        transAmountLabel = new JLabel("Transaction Amount");
        transAmountLabel.setBounds(20, 80, 150, 30);

        transAmountField = new JTextField(10);
        transAmountField.setBounds(200, 80, 150, 30);

        displayArea = new JTextArea();
        displayArea.setBounds(450, 40, 150, 150);

        transTypeLabel = new JLabel("Transaction Type");
        transTypeLabel.setBounds(20, 130, 150, 30);

        transactionType = new JComboBox<String>(transaction);
        transactionType.setBounds(200, 130, 150, 30);

        confirmButton = new JButton("Ok");
        confirmButton.setBounds(200, 220, 120, 30);


        finishButton = new JButton("Finish");
        finishButton.setBounds(340, 220, 120, 30);

        exitButton = new JButton("exit");
        exitButton.setBounds(400, 300, 120, 30);
    }

    //Addign components to the panel
    public void addComponentsToPanel() {
        mainPanel.add(accNumLabel);
        mainPanel.add(accNumField);
        mainPanel.add(displayArea);
        mainPanel.add(transAmountLabel);
        mainPanel.add(transAmountField);
        mainPanel.add(transTypeLabel);
        mainPanel.add(transactionType);
        mainPanel.add(confirmButton);
        mainPanel.add(finishButton);
        mainPanel.add(exitButton);
    }

    //Setting window properties
    public void setWindowProperties() {
        setLayout(null);
        add(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLayout(null);
        setVisible(true);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    //Registering Listeners
    public void registerListeners() {
        confirmButton.addActionListener(this);
        finishButton.addActionListener(this);
        exitButton.addActionListener(this);
    }

    public boolean validateFields() {
        return accNumField.getText().isEmpty() ||
                transAmountField.getText().isEmpty();
    }


    public void displayInfo(Transaction transaction) {
        String output = "";
        output += "Account Number: " + transaction.getAccountNumber()
                + "\nTransaction Type: " + transaction.getTransactionType()
                + "\nTransaction Amount: " + transaction.getAccountNumber()
                + "\nAccount Balance: " + transaction.getAccountBalance();

        displayArea.setText(output);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == confirmButton) {
            if (validateFields()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields");
            } else {
                String accountNumber = accNumField.getText();
                double transactionAmount = Double.parseDouble(transAmountField.getText());
                String transactionTypes = (String) transactionType.getSelectedItem();
                Client client = new Client();
                client.sendAction("Process Transaction");
                client.sendTransaction(new Transaction(accountNumber, transactionAmount, transactionTypes));
                //display this data on screen
                displayInfo(client.receiveResponse());
            }
        }

        if (e.getSource() == exitButton) {//ensures server connection is kept unless client closes
            Client client = new Client();
            Transaction transaction = new Transaction();
            transaction.setTransactionType("EXIT");
            // Send Transaction object to server and close connections
            client.sendAction("Process Transaction");
            client.sendTransaction(transaction);
            client.sendAction("exit");
            client.closeConnection();
        }

        if(e.getSource() == finishButton){
            // Reset ATM screen when finishButton is clicked
            accNumField.setText("");
            transAmountField.setText("");
            transactionType.setSelectedIndex(0); // Set default value to 'Deposit'
            displayArea.setText("");
        }
    }

}
