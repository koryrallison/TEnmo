package com.techelevator.tenmo;

import com.techelevator.tenmo.enums.TransferStatus;
import com.techelevator.tenmo.enums.TransferType;
import com.techelevator.tenmo.model.*;
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
    private Account activeAccount;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleUserRegister();
            } else if (menuSelection == 2) {
                handleLogin();
                accountLoginMenu();
                if (currentUser != null && activeAccount != null) {
                    mainMenu();
                } else {
                    handleLogout();
                }
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleUserRegister() {
        System.out.println("Please register a new user login and name your first account");
        RegistrationCredentials credentials = consoleService.promptForRegistration();
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

    private void handleLogout() {
        currentUser = null;
        activeAccount = null;
        transactionService.setAuthToken(null);
        transactionService.setActiveAccountId(null);
    }

    private void handleAccountRegister(){
        String accountName = consoleService.promptForString("Please enter a name for the account: ");
        Account newAccount = transactionService.newAccount(accountName);
        if (newAccount != null) {
            System.out.println("Registration successful. You've opened a new account.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void accountLoginMenu() {
        System.out.println("-------------------------------");
        System.out.println("           Accounts            ");
        System.out.println("-------------------------------");
        for (Account account : transactionService.getCurrentUserAccounts()) {
            System.out.println("Account ID: " + account.getAccount_id() +
                    "     Account Name: " + account.getAccount_name() +
                    "     Balance: " + account.getBalance());
        }
        accountSelectMenu();
    }

    private void accountListMenu(){
        System.out.println("-------------------------------");
        System.out.println("           Accounts            ");
        System.out.println("-------------------------------");
        for (Account account : transactionService.getCurrentUserAccounts()) {
            System.out.println("Account ID: " + account.getAccount_id() +
                    "     Account Name: " + account.getAccount_name() +
                    "     Balance: " + account.getBalance());
        }
        if (activeAccount != null) {System.out.println("Active Account: " + activeAccount.getAccount_name());}
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printAccountMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                accountSelectMenu();
                break;
            } else if (menuSelection == 2) {
                handleAccountRegister();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void accountSelectMenu(){
        int accountSelection = -1;
        while (accountSelection != 0) {
            accountSelection = consoleService.promptForInt("Please enter an Account ID from the list above (or 0 to Exit): ");
            if (accountSelection >= 1) {
                    activeAccount = transactionService.verifyAccountId(accountSelection);
                    transactionService.setActiveAccountId(activeAccount.getAccount_id());
                    if (activeAccount == null) {
                        consoleService.printErrorMessage();
                    }
                    else break;
            } else if (accountSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (true) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                accountListMenu();
            } else if (menuSelection == 3) {
                viewTransferHistory();
            } else if (menuSelection == 4) {
                viewPendingRequests();
            } else if (menuSelection == 5) {
                sendBucks();
            } else if (menuSelection == 6) {
                requestBucks();
            } else if (menuSelection == 0) {
                handleLogout();
                break;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

	private void viewCurrentBalance() {
        System.out.println("Your current total balance accross all accounts is: $" + transactionService.getUserBalance());
	}

	private void viewTransferHistory() {
        System.out.println("-------------------------------");
        System.out.println("Transfer History");
        System.out.println("ID     From/To          Amount");
        System.out.println("-------------------------------");

        for (Transfer transfer : transactionService.getTransferHistory()) {
            if (transfer.getAccount_from() == activeAccount.getAccount_id()) {
                System.out.println("Transfer ID: " + transfer.getTransfer_id()
                        + "     To: " + transactionService.getUsernameFromAccountId(transfer.getAccount_to())
                        + "     $" + transfer.getAmount());
            } else if (transfer.getAccount_to() == activeAccount.getAccount_id()) {
                System.out.println("Transfer ID: " + transfer.getTransfer_id()
                        + "     From: " + transactionService.getUsernameFromAccountId(transfer.getAccount_from())
                        + "       $" + transfer.getAmount());
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
                        + "     Request From: " + transactionService.getUsernameFromAccountId(transfer.getAccount_to())
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
                System.out.println("From: " + transactionService.getUsernameFromAccountId(transfer.getAccount_from()) + " (" + transactionService.getAccountNameFromAccountId(transfer.getAccount_from()) + ")");
                System.out.println("To: " + transactionService.getUsernameFromAccountId(transfer.getAccount_to()) + " (" + transactionService.getAccountNameFromAccountId(transfer.getAccount_to()) + ")");
                System.out.println("Type: " + TransferType.getTypeById(transfer.getTransferType()));
                System.out.println("Status: " + TransferStatus.getStatusById(transfer.getTransferStatus()));
                System.out.println("Amount: $" + transfer.getAmount());
                break;
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
                System.out.println("Transaction " + transferSelection + " Approved!");
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
                System.out.println("Transaction " + transferSelection + " Rejected!");
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
                System.out.println("-------------------------------");
                System.out.println("           Accounts            ");
                System.out.println("-------------------------------");
                for (Account account : transactionService.getAccountsByUserId(userSelection)) {
                    System.out.println("Account ID: " + account.getAccount_id() +
                            "     Account Name: " + account.getAccount_name());
                }
                int accountSelection = consoleService.promptForInt("Please enter an Account ID from the list above (or 0 to Exit): ");
                BigDecimal amount = consoleService.promptForBigDecimal("Please enter an amount of money to send to " + transactionService.getUsernameFromUserId(userSelection) + ": ");
                Transfer transfer = new Transfer();
                transfer.setAccount_from(activeAccount.getAccount_id());
                transfer.setAccount_to(accountSelection);
                transfer.setAmount(amount);
                transactionService.createOutboundTransfer(transfer);
                break;
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
                System.out.println("-------------------------------");
                System.out.println("           Accounts            ");
                System.out.println("-------------------------------");
                for (Account account : transactionService.getAccountsByUserId(userSelection)) {
                    System.out.println("Account ID: " + account.getAccount_id() +
                            "     Account Name: " + account.getAccount_name());
                }
                int accountSelection = consoleService.promptForInt("Please enter an Account ID from the list above (or 0 to Exit): ");
                BigDecimal amount = consoleService.promptForBigDecimal("Please enter an amount of money to request from " + transactionService.getUsernameFromUserId(userSelection) + ": ");
                Transfer transfer = new Transfer();
                transfer.setAccount_from(accountSelection);
                transfer.setAmount(amount);
                transactionService.createTransferRequest(transfer);
                break;
            } else if (userSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
	}
}
