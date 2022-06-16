package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transfers;

public interface TransfersDao {

    public List<Transfers> getAllTransfers(int userId);
    public Transfers getTransferById(int transactionId);
    public String sendTransfer(int userFrom, int userTo, BigDecimal amount);
    public String requestTransfer(int userFrom, int userTo, BigDecimal amount);
    public List<Transfers> getPendingRequests(int userId);
    public String updateTransferRequest(Transfers transfer, int statusId);

}