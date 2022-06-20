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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;

@RestController
//@PreAuthorize("isAuthenticated()")
//@PreAuthorize("#username == authentication.principal.username")
public class TransactionController {

    private UserDao userDao;
    private AccountDao accountDao;
    private TransferDao transferDao;

    @Autowired
    public TransactionController(UserDao userDao, AccountDao accountDao, TransferDao transferDao) {
        this.userDao = userDao;
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/myaccount", method = RequestMethod.GET)
    public int myAccount(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findByUserId(user.getUser_id());
        return account.getAccount_id();
    }

    @RequestMapping(path = "/account/{accountId}", method = RequestMethod.GET)
    public String getUsernameOfAccount(@PathVariable long accountId, Principal principal) {
        Account account = accountDao.findById(accountId);
        User user = userDao.findById(account.getUserId());
        return user.getUsername();
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.GET)
    public String getUsernameOfUser(@PathVariable long userId, Principal principal) {
        User user = userDao.findById(userId);
        return user.getUsername();
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal balance(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findByUserId(user.getUser_id());
        return account.getBalance();
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
        return userDao.findAllOthers(user.getUser_id()).toArray(new User[0]);
    }

    @RequestMapping(path = "/transfer/{id}", method = RequestMethod.GET)
    public Transfer findTransfer(@PathVariable long transferId, Principal principal) throws TransferNotFoundException {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findByUserId(user.getUser_id());
        Transfer transfer = transferDao.findById(transferId);
        if (transfer.getAccount_from() == account.getAccount_id() || transfer.getAccount_to() == account.getAccount_id()){
            return transfer;
        }
        else throw new TransferNotFoundException();
    }

    @RequestMapping(path = "/transfer/send", method = RequestMethod.POST)
    public Transfer send(@Valid @RequestBody Transfer transfer, Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        Account accountFrom = accountDao.findByUserId(user.getUser_id());
        Account accountTo = accountDao.findByUserId(transfer.getAccount_to());
        int newTransferId = transferDao.create(JdbcTransferDao.OUTBOUND_TYPE_ID, accountFrom.getAccount_id(), accountTo.getAccount_id(), transfer.getAmount());
        Transfer newTransfer = transferDao.approve(transferDao.findById(newTransferId));
        if (newTransfer != null) {
            return newTransfer;
        }
        else throw new RecoverableDataAccessException("Failed to approve transaction.");
    }

    @RequestMapping(path = "/transfer/request", method = RequestMethod.POST)
    public Transfer request(@Valid @RequestBody Transfer transfer, Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        Account accountFrom = accountDao.findByUserId(transfer.getAccount_from());
        Account accountTo = accountDao.findByUserId(user.getUser_id());
        return transferDao.findById(transferDao.create(JdbcTransferDao.INBOUND_TYPE_ID, accountFrom.getAccount_id(), accountTo.getAccount_id(), transfer.getAmount()));
    }

    @RequestMapping(path = "/transfer/{transferId}/approve", method = RequestMethod.PUT)
    public boolean approveTransfer(@PathVariable long transferId, Principal principal) throws TransferNotFoundException {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findByUserId(user.getUser_id());
        Transfer transfer = transferDao.findById(transferId);
        if (transfer.getAccount_from() == account.getAccount_id()){
            transferDao.approve(transfer);
            return true;
        }
        else return false;
    }

    @RequestMapping(path = "/transfer/{transferId}/reject", method = RequestMethod.PUT)
    public boolean rejectTransfer(@PathVariable long transferId, Principal principal) throws TransferNotFoundException {
        User user = userDao.findByUsername(principal.getName());
        Account account = accountDao.findByUserId(user.getUser_id());
        Transfer transfer = transferDao.findById(transferId);
        if (transfer.getAccount_from() == account.getAccount_id()){
            transferDao.reject(transfer);
            return true;
        }
        else return false;
    }
}
