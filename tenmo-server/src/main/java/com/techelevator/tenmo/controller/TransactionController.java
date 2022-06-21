package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.TransferNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
//@PreAuthorize("#username == authentication.principal.username")
public class TransactionController {

    private final UserDao userDao;
    private final AccountDao accountDao;
    private final TransferDao transferDao;

    @Autowired
    public TransactionController(UserDao userDao, AccountDao accountDao, TransferDao transferDao) {
        this.userDao = userDao;
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal balance(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return userDao.findUserBalance(user.getUser_id());
    }

    @RequestMapping(path = "/history", method = RequestMethod.GET)
    public Transfer[] history(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return transferDao.findByUser(user.getUser_id()).toArray(new Transfer[0]);
    }

    @RequestMapping(path = "/pending", method = RequestMethod.GET)
    public Transfer[] pending(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return transferDao.findInboundPending(user.getUser_id()).toArray(new Transfer[0]);
    }

    @RequestMapping(path = "/recipients", method = RequestMethod.GET)
    public User[] recipients(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return userDao.findAll().toArray(new User[0]);
    }

    @RequestMapping(path = "/myaccounts", method = RequestMethod.GET)
    public Account[] myAccounts(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return accountDao.findByUserId(user.getUser_id()).toArray(new Account[0]);
    }

    @RequestMapping(path = "changeaccount/{accountId}", method = RequestMethod.GET)
    public Account verifyAccount(@PathVariable int accountId, Principal principal) throws Exception {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findById(accountId);
        if (account.getUser_id() == user.getUser_id()) {
            return account;
        } else {
            throw new Exception("Could not find that Account ID among your accounts.");
        }
    }

    @RequestMapping(path = "/user/{userId}/accounts", method = RequestMethod.GET)
    public Account[] getAccountsByUsername(@PathVariable long userId, Principal principal) {
        User user = userDao.findById(userId);
        return accountDao.findByUserId(user.getUser_id()).toArray(new Account[0]);
    }

    @RequestMapping(path = "/account/{accountId}/username", method = RequestMethod.GET)
    public String getUsernameOfAccount(@PathVariable long accountId, Principal principal) {
        Account account = accountDao.findById(accountId);
        User user = userDao.findById(account.getUser_id());
        return user.getUsername();
    }

    @RequestMapping(path = "/account/{accountId}/accountname", method = RequestMethod.GET)
    public String getNameOfAccount(@PathVariable long accountId, Principal principal) {
        Account account = accountDao.findById(accountId);
        return account.getAccount_name();
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.GET)
    public String getUsernameOfUser(@PathVariable long userId, Principal principal) {
        User user = userDao.findById(userId);
        return user.getUsername();
    }

    @RequestMapping(path = "/transfer/{transferId}", method = RequestMethod.GET)
    public Transfer findTransfer(@PathVariable long transferId, Principal principal, @RequestHeader("current-account") int accountId) throws TransferNotFoundException {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findById(accountId);
        Transfer transfer = transferDao.findById(transferId);
        if (transfer.getAccount_from() == account.getAccount_id() || transfer.getAccount_to() == account.getAccount_id()){
            return transfer;
        }
        else throw new TransferNotFoundException();
    }

    @RequestMapping(path = "/transfer/send", method = RequestMethod.POST)
    public Transfer send(@Valid @RequestBody Transfer transfer, Principal principal, @RequestHeader("current-account") int accountId) {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findById(accountId);
        Transfer transferHolder = new Transfer(JdbcTransferDao.SEND_TYPE_ID, JdbcTransferDao.PENDING_STATUS_ID, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
        int newTransferId = transferDao.create(transferHolder);
        Transfer newTransfer = transferDao.approve(transferDao.findById(newTransferId));
        if (newTransfer != null && newTransferId != -1) {
            return newTransfer;
        }
        else throw new RecoverableDataAccessException("Failed to create transaction.");
    }

    @RequestMapping(path = "/transfer/request", method = RequestMethod.POST)
    public Transfer request(@Valid @RequestBody Transfer transfer, Principal principal, @RequestHeader("current-account") int accountId) {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findById(accountId);
        Transfer transferHolder = new Transfer(JdbcTransferDao.REQUEST_TYPE_ID, JdbcTransferDao.PENDING_STATUS_ID, transfer.getAccount_from(), account.getAccount_id(), transfer.getAmount());
        int newTransferId = transferDao.create(transferHolder);
        Transfer newTransfer = transferDao.findById(newTransferId);
        if (newTransfer != null && newTransferId != -1) {
            return newTransfer;
        } else throw new RecoverableDataAccessException("Failed to create transaction.");
    }

    @RequestMapping(path = "/transfer/{transferId}/approve", method = RequestMethod.PUT)
    public Transfer approveTransfer(@PathVariable long transferId, Principal principal, @RequestHeader("current-account") int accountId) throws Exception {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findById(accountId);
        Transfer transfer = transferDao.findById(transferId);
        if (transferValidation(user, account, transfer)){
            transfer = transferDao.approve(transfer);
        }
        return transfer;
    }

    @RequestMapping(path = "/transfer/{transferId}/reject", method = RequestMethod.PUT)
    public Transfer rejectTransfer(@PathVariable long transferId, Principal principal, @RequestHeader("current-account") int accountId) throws Exception {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findById(accountId);
        Transfer transfer = transferDao.findById(transferId);
        if (transferValidation(user, account, transfer)){
            transfer = transferDao.reject(transfer);
        }
        return transfer;
    }

    public boolean transferValidation(User user, Account account, Transfer transfer) throws Exception {
        if (user.getUser_id() != account.getUser_id()) {
            throw new Exception("User does not own this account");
        } else if (account.getAccount_id() != transfer.getAccount_from() && account.getAccount_id() != transfer.getAccount_to()) {
            throw new Exception("Account is not associated with this transfer");
        } else if (transfer.getTransferType() == JdbcTransferDao.REQUEST_TYPE_ID && account.getAccount_id() != transfer.getAccount_from()) {
            throw new Exception("Account is not authorized to approve or reject this transfer");
        } else if (transfer.getTransferStatus() == JdbcTransferDao.INVALID_STATUS_ID){
            throw new Exception("Transaction has already been invalidated");
        } else if (transfer.getTransferStatus() == JdbcTransferDao.APPROVED_STATUS_ID){
            throw new Exception("Transaction has already been approved");
        } else if (transfer.getTransferStatus() == JdbcTransferDao.REJECTED_STATUS_ID){
            throw new Exception("Transaction has already been rejected");
        } else if (transfer.getTransferStatus() == JdbcTransferDao.COMPLETED_STATUS_ID){
            throw new Exception("Transaction has already been completed");
        } else if (transfer.getAccount_from() == transfer.getAccount_to()) {
            transferDao.invalidate(transfer);
            throw new Exception("Account From and Account To must be different.");
        }else if (transfer.getAmount().compareTo(BigDecimal.ZERO) < 0){
            transferDao.invalidate(transfer);
            throw new Exception("Cannot transfer a zero or negative amount");
        } else if (accountDao.findById(transfer.getAccount_from()).getBalance().compareTo(transfer.getAmount()) < 0){
            transferDao.invalidate(transfer);
            throw new Exception("Insufficient funds to complete transfer");
        } else return true;
    }
}
