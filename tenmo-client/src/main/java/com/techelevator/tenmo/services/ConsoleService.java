package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.RegistrationCredentials;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your account list");
        System.out.println("3: View your past transfers");
        System.out.println("4: View your pending requests");
        System.out.println("5: Send TE bucks");
        System.out.println("6: Request TE bucks");
        System.out.println("0: Log Out");
        System.out.println();
    }

    public void printAccountMenu() {
        System.out.println();
        System.out.println("1: Select Active Account");
        System.out.println("2: Create New Account");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printTransferHistoryMenu() {
        System.out.println();
        System.out.println("1: Lookup Transfer Details By Id");
        System.out.println("2: Advanced Transfer Search");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printPendingTransferMenu() {
        System.out.println();
        System.out.println("1: Approve Transfer by ID");
        System.out.println("2: Reject Transfer by ID");
        System.out.println("0: Exit");
        System.out.println();
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public RegistrationCredentials promptForRegistration() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        String account_name = promptForString("Account Name: ");
        return new RegistrationCredentials(username, password, account_name);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                BigDecimal input = new BigDecimal(scanner.nextLine());
                if (input.scale() > 2) {
                    throw new NumberFormatException("Please enter a number with no more than 2 digits after the decimal");
                } else {
                    return input;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

}
