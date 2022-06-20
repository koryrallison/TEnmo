package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransactionService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final TransactionService transactionService = new TransactionService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        transactionService.setAuthToken(currentUser.getToken());
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

	private void viewCurrentBalance() {
        System.out.println("Your current account balance is: $" + transactionService.getAccountBalance());
	}

	private void viewTransferHistory() {
        System.out.println("-------------------------------");
        System.out.println("Transfer History");
        System.out.println("ID     From/To          Amount");
        System.out.println("-------------------------------");

        for (Transfer transfer : transactionService.getTransferHistory()) {
            if (transfer.getTransferType() == 1) {
                System.out.println("Transfer ID: " + transfer.getTransfer_id()
                        + "   From: " + transactionService.getUsernameFromAccountId(transfer.getAccount_from())
                        + "     $" + transfer.getAmount());
            } else if (transfer.getTransferType() == 2) {
                System.out.println("Transfer ID: " + transfer.getTransfer_id()
                        + "     To: " + transactionService.getUsernameFromAccountId(transfer.getAccount_to())
                        + "     $" + transfer.getAmount());
            } else {
                System.err.println("Error: Unknown TransferType Detected in Transfer ID: " + transfer.getTransfer_id());
            }
        }
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printTransferHistoryMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                lookupTransferById();
            } else if (menuSelection == 2) {
                continue;
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
	}

	private void viewPendingRequests() {
            System.out.println("-------------------------------");
            System.out.println("      Pending Requests         ");
            System.out.println("ID          To           Amount");
            System.out.println("-------------------------------");

            for (Transfer transfer : transactionService.getPendingTransfers()) {
                System.out.println("Transfer ID: " + transfer.getTransfer_id()
                        + "     To: " + transactionService.getUsernameFromAccountId(transfer.getAccount_to())
                        + "     $" + transfer.getAmount());
            }
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printPendingTransferMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                approveTransferById();
            } else if (menuSelection == 2) {
                rejectTransferById();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
	}

    private void lookupTransferById(){
        long transferSelection = -1;
        while (transferSelection != 0) {
            transferSelection = consoleService.promptForInt("Please enter a Transfer ID from the list above (or 0 to Exit): ");
            if (transferSelection >= 1) {
                Transfer transfer = transactionService.getTransferById(transferSelection);
                System.out.println("-------------------------------");
                System.out.println("Transfer Details");
                System.out.println("-------------------------------");
                System.out.println("Id: " + transfer.getTransfer_id());
                System.out.println("From: " + transfer.getAccount_from());
                System.out.println("To: " + transfer.getAccount_to());
                System.out.println("Type: " + transfer.getTransferType());
                System.out.println("Status: " + transfer.getTransferStatus());
                System.out.println("Amount: $" + transfer.getAmount());
            } else if (transferSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void approveTransferById(){
        long transferSelection = -1;
        while (transferSelection != 0) {
            transferSelection = consoleService.promptForInt("Please enter a Transfer ID from the list above (or 0 to Exit): ");
            if (transferSelection >= 1) {
                transactionService.approveTransfer(transferSelection);
            } else if (transferSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void rejectTransferById(){
        long transferSelection = -1;
        while (transferSelection != 0) {
            transferSelection = consoleService.promptForInt("Please enter a Transfer ID from the list above (or 0 to Exit): ");
            if (transferSelection >= 1) {
                transactionService.rejectTransfer(transferSelection);
            } else if (transferSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

	private void sendBucks() {
        System.out.println("-------------------------------");
        System.out.println("             Users             ");
        System.out.println("-------------------------------");

        for (User user : transactionService.getOtherUsers()) {
            System.out.println("User ID: " + user.getUser_id() + "     Username: " + user.getUsername());
        }
        int userSelection = -1;
        while (userSelection != 0) {
            userSelection = consoleService.promptForInt("Please enter a User ID from the list above to send money to (or 0 to Exit): ");
            if (userSelection >= 1) {
                BigDecimal amount = consoleService.promptForBigDecimal("Please enter an amount of money to send to " + transactionService.getUsernameFromUserId(userSelection) + ": ");
                Transfer transfer = new Transfer();
                transfer.setAccount_to(userSelection);
                transfer.setAmount(amount);
                transactionService.createOutboundTransfer(transfer);
            } else if (userSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
	}

	private void requestBucks() {
        System.out.println("-------------------------------");
        System.out.println("             Users             ");
        System.out.println("-------------------------------");

        for (User user : transactionService.getOtherUsers()) {
            System.out.println("User ID: " + user.getUser_id() + "     Username: " + user.getUsername());
        }
        int userSelection = -1;
        while (userSelection != 0) {
            userSelection = consoleService.promptForInt("Please enter a User ID from the list above to request money from (or 0 to Exit): ");
            if (userSelection >= 1) {
                BigDecimal amount = consoleService.promptForBigDecimal("Please enter an amount of money to request from " + transactionService.getUsernameFromUserId(userSelection) + ": ");
                Transfer transfer = new Transfer();
                transfer.setAccount_from(userSelection);
                transfer.setAmount(amount);
                transactionService.createTransferRequest(transfer);
            } else if (userSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
	}

}
