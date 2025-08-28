import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class BankingSystemGUI {
    private JFrame frame;
    private JTextField nameField, idField, amountField;
    private JTextArea displayArea;

    public BankingSystemGUI() {
        frame = new JFrame("Simple Banking System");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(20, 20, 80, 25);
        frame.add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(100, 20, 200, 25);
        frame.add(nameField);

        JLabel idLabel = new JLabel("Customer ID:");
        idLabel.setBounds(20, 60, 80, 25);
        frame.add(idLabel);

        idField = new JTextField();
        idField.setBounds(100, 60, 200, 25);
        frame.add(idField);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(20, 100, 80, 25);
        frame.add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(100, 100, 200, 25);
        frame.add(amountField);

        JButton createButton = new JButton("Create Account");
        createButton.setBounds(20, 140, 140, 30);
        frame.add(createButton);

        JButton depositButton = new JButton("Deposit");
        depositButton.setBounds(180, 140, 120, 30);
        frame.add(depositButton);

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setBounds(20, 180, 140, 30);
        frame.add(withdrawButton);

        JButton balanceButton = new JButton("Check Balance");
        balanceButton.setBounds(180, 180, 140, 30);
        frame.add(balanceButton);

        displayArea = new JTextArea();
        displayArea.setBounds(20, 220, 340, 120);
        displayArea.setEditable(false);
        frame.add(displayArea);

        createButton.addActionListener(e -> createAccount());
        depositButton.addActionListener(e -> depositMoney());
        withdrawButton.addActionListener(e -> withdrawMoney());
        balanceButton.addActionListener(e -> checkBalance());

        frame.setVisible(true);
    }

    private void createAccount() {
        String name = nameField.getText();
        String id = idField.getText();
        if (name.isEmpty() || id.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter Name and Customer ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String data = id + "," + name + ",0.0";
        if (saveAccountToFile(data)) {
            displayArea.setText("Account Created Successfully!");
            JOptionPane.showMessageDialog(frame, "Account Created Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        } else {
            displayArea.setText("Error saving account.");
            JOptionPane.showMessageDialog(frame, "Error saving account.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void depositMoney() {
        String id = idField.getText();
        String amountText = amountField.getText();
        if (id.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter Customer ID and Amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (updateBalanceInFile(id, Double.parseDouble(amountText), true)) {
            JOptionPane.showMessageDialog(frame, "Amount Deposited Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        }
    }

    private void withdrawMoney() {
        String id = idField.getText();
        String amountText = amountField.getText();
        if (id.isEmpty() || amountText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter Customer ID and Amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (updateBalanceInFile(id, Double.parseDouble(amountText), false)) {
            JOptionPane.showMessageDialog(frame, "Amount Withdrawn Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        }
    }

    private void checkBalance() {
        String id = idField.getText();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter Customer ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String balance = getBalanceFromFile(id);
        displayArea.setText(balance);
        JOptionPane.showMessageDialog(frame, balance, "Balance Details", JOptionPane.INFORMATION_MESSAGE);
        clearFields();
    }

    private boolean saveAccountToFile(String data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("accounts.txt", true))) {
            writer.write(EncryptionUtil.encrypt(data));
            writer.newLine();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean updateBalanceInFile(String id, double amount, boolean isDeposit) {
        File inputFile = new File("accounts.txt");
        File tempFile = new File("temp.txt");
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String decrypted = EncryptionUtil.decrypt(line);
                String[] parts = decrypted.split(",");
                if (parts[0].equals(id)) {
                    found = true;
                    double balance = Double.parseDouble(parts[2]);
                    if (isDeposit) {
                        balance += amount;
                        displayArea.setText("Deposited: " + amount);
                    } else {
                        if (amount > balance) {
                            displayArea.setText("Insufficient Balance.");
                            JOptionPane.showMessageDialog(frame, "Insufficient Balance.", "Error", JOptionPane.ERROR_MESSAGE);
                            writer.write(line);
                            writer.newLine();
                            return false;
                        }
                        balance -= amount;
                        displayArea.setText("Withdrawn: " + amount);
                    }
                    String newData = id + "," + parts[1] + "," + balance;
                    writer.write(EncryptionUtil.encrypt(newData));
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            displayArea.setText("Error updating file.");
            JOptionPane.showMessageDialog(frame, "Error updating file.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!found) {
            displayArea.setText("Account not found.");
            JOptionPane.showMessageDialog(frame, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            inputFile.delete();
            tempFile.renameTo(inputFile);
            return true;
        }
    }

    private String getBalanceFromFile(String id) {
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String decrypted = EncryptionUtil.decrypt(line);
                String[] parts = decrypted.split(",");
                if (parts[0].equals(id)) {
                    return "Account Balance: " + parts[2];
                }
            }
        } catch (IOException e) {
            return "Error reading file.";
        }
        return "Account not found.";
    }

    private void clearFields() {
        nameField.setText("");
        idField.setText("");
        amountField.setText("");
    }

    public static void main(String[] args) {
        new BankingSystemGUI();
    }
}

class EncryptionUtil {
    private static final String KEY = "SimpleKey";

    public static String encrypt(String data) {
        byte[] keyBytes = KEY.getBytes();
        byte[] dataBytes = data.getBytes();
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] ^= keyBytes[i % keyBytes.length];
        }
        return Base64.getEncoder().encodeToString(dataBytes);
    }

    public static String decrypt(String encryptedData) {
        byte[] dataBytes = Base64.getDecoder().decode(encryptedData);
        byte[] keyBytes = KEY.getBytes();
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] ^= keyBytes[i % keyBytes.length];
        }
        return new String(dataBytes);
    }
}
