package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfers;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Scanner;

public class TransferService {

    private String baseUrl;
    private RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    public TransferService(String url, AuthenticatedUser currentUser){
        this.currentUser = currentUser;
        baseUrl = url;
    }

    public Transfers[] listOfTransfers(){
        Transfers [] transfersList = null;
        try {
            transfersList = restTemplate.exchange(baseUrl + "account/transfers/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfers[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Transfers\r\n" +
                    "ID          From/To                 Amount\r\n" +
                    "-------------------------------------------\r\n");
            String fromOrTo = "";
            String name = "";
            for (Transfers transfer : transfersList){
                if(currentUser.getUser().getId() == transfer.getAccountFrom()) {
                    fromOrTo = "From: ";
                    name = transfer.getUserTo();
                }
                else {
                    fromOrTo = "To: ";
                    name = transfer.getUserFrom();
                }
                System.out.println(transfer.getTransferId() + "\t\t" + fromOrTo + name + "\t\t$" + transfer.getAmount());
            }
            System.out.print("Please enter transfer ID to view details (0 to cancel): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if(Integer.parseInt(input) != 0) {
                boolean foundTransferId = false;
                for (Transfers transfer : transfersList) {
                    if (Integer.parseInt(input) == transfer.getTransferId()) {
                        Transfers tempRest = restTemplate.exchange(baseUrl + "transfers/" + transfer.getTransferId(), HttpMethod.GET, makeAuthEntity(), Transfers.class).getBody();
                        foundTransferId = true;
                        System.out.println("--------------------------------------------\r\n" +
                                "Transfer Details\r\n" +
                                "--------------------------------------------\r\n" +
                                " Id: "+ tempRest.getTransferId() + "\r\n" +
                                " From: " + tempRest.getUserFrom() + "\r\n" +
                                " To: " + tempRest.getUserTo() + "\r\n" +
                                " Type: " + tempRest.getTransferType() + "\r\n" +
                                " Status: " + tempRest.getTransferStatus() + "\r\n" +
                                " Amount: $" + tempRest.getAmount());

                    }
                }
                if(!foundTransferId) {
                    System.out.println("Not A valid transfer ID");
                }

            }
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }
        return transfersList;
    }


    public void sendBucks() {
        User[] users = null;
        Transfers transfer = new Transfers();
        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(baseUrl + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Users\r\n" +
                    "ID\t\tName\r\n" +
                    "-------------------------------------------");
            for (User user : users) {
                if (!Objects.equals(user.getId(), currentUser.getUser().getId())) {
                    System.out.println(user.getId() + "\t\t" + user.getUsername());
                }
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Enter ID of user you are sending to (0 to cancel): ");
            transfer.setAccountTo(Integer.parseInt(scanner.nextLine()));
            transfer.setAccountFrom(Math.toIntExact(currentUser.getUser().getId()));
            if(transfer.getAccountTo() != 0) {
                System.out.print("Enter amount: ");
                try {
                    transfer.setAmount(new BigDecimal(Double.parseDouble(scanner.nextLine())));
                } catch (NumberFormatException e) {
                    System.out.println("Error when entering amount");
                }
                String output = restTemplate.exchange(baseUrl + "transfer", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Bad input.");
        }

    }

    public void requestBucks() {
        User[] users = null;
        Transfers transfer = new Transfers();
        try {
            Scanner scanner = new Scanner(System.in);
            users = restTemplate.exchange(baseUrl + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Users\r\n" +
                    "ID\t\tName\r\n" +
                    "-------------------------------------------");
            for (User i : users) {
                if (i.getId() != currentUser.getUser().getId()) {
                    System.out.println(i.getId() + "\t\t" + i.getUsername());
                }
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Enter ID of user you are requesting from (0 to cancel): ");
            transfer.setAccountTo(Math.toIntExact(currentUser.getUser().getId()));
            transfer.setAccountFrom(Integer.parseInt(scanner.nextLine()));
            if (transfer.getAccountTo() != 0) {
                System.out.print("Enter amount: ");
                try {
                    transfer.setAmount(new BigDecimal(Double.parseDouble(scanner.nextLine())));
                } catch (NumberFormatException e) {
                    System.out.println("Error when entering amount");
                }
                String output = restTemplate.exchange(baseUrl + "request", HttpMethod.POST, makeTransferEntity(transfer), String.class).getBody();
                System.out.println(output);
            }
        } catch (Exception e) {
            System.out.println("Bad input.");
        }

    }

    public Transfers[] listOfTransferRequests() {
        Transfers [] output = null;
        String results;
        try {
            output = restTemplate.exchange(baseUrl + "request/" + currentUser.getUser().getId(), HttpMethod.GET, makeAuthEntity(), Transfers[].class).getBody();
            System.out.println("-------------------------------------------\r\n" +
                    "Pending Transfers\r\n" +
                    "ID          From/To                 Amount\r\n" +
                    "-------------------------------------------\r\n");
            String fromOrTo = "";
            String name = "";
            for (Transfers i : output) {
                if (currentUser.getUser().getId() == i.getAccountFrom()) {
                    fromOrTo = "From: ";
                    name = i.getUserTo();
                } else {
                    fromOrTo = "To: ";
                    name = i.getUserFrom();
                }
                System.out.println(i.getTransferId() +"\t\t" + fromOrTo + name + "\t\t$" + i.getAmount());
            }
            System.out.print("-------------------------------------------\r\n" +
                    "Please enter transfer ID to approve/reject (0 to cancel): ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            if (Integer.parseInt(input) != 0) {
                boolean foundTransferId = false;
                for (Transfers i : output) {
                    if (i.getAccountTo() != currentUser.getUser().getId()) {
                        if (Integer.parseInt(input) == i.getTransferId()) {
                            System.out.print("-------------------------------------------\r\n" +
                                    i.getTransferId() +"\t\t" + fromOrTo + name + "\t\t$" + i.getAmount() + "\r\n" +
                                    "1: Approve\r\n" +
                                    "2: Reject\r\n" +
                                    "0: Don't approve or reject\r\n" +
                                    "--------------------------\r\n" +
                                    "Please choose an option: ");
                            try {
                                int id = 1 + Integer.parseInt(scanner.nextLine());
                                if (id != 1) {
                                    results = restTemplate.exchange(baseUrl + "transfer/status/" + id, HttpMethod.PUT, makeTransferEntity(i), String.class).getBody();
                                    System.out.println(results);
                                    foundTransferId = true;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Not a valid transfer option");
                            }
                            if (!foundTransferId) {
                                System.out.println("Not a valid transfer ID");
                            }
                        }
                    } else {
                        System.out.println("You can not approve/reject your own request.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Something went wrong... Opps! We have all your money now!");
        }
        return output;
    }

    public User[] getUsers() {
        User[]  users = null;
        try {
            users = restTemplate.exchange(baseUrl + "listusers", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();
            for(User user: users) {
                System.out.println(user);

            }
        } catch (RestClientResponseException e) {
            System.out.println("Error getting users");
        }
        return users;
    }



    private HttpEntity<Transfers> makeTransferEntity(Transfers transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Transfers> entity = new HttpEntity<>(transfer, headers);
        return entity;
    }

    private HttpEntity makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity entity = new HttpEntity<>(headers);
        return entity;
    }
}
