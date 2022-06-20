package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JdbcTransferDao implements TransferDao {

    public static final int PENDING_STATUS_ID = 1;
    public static final int APPROVED_STATUS_ID = 2;
    public static final int REJECTED_STATUS_ID = 3;
    public static final int INBOUND_TYPE_ID = 1;
    public static final int OUTBOUND_TYPE_ID = 2;

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    @Autowired
    public JdbcTransferDao(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Transfer findById(long transferId) {
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE transfer_id = ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, transferId);
        if (rowSet.next()){
            return mapRowToTransfer(rowSet);
        }
        throw new RecoverableDataAccessException("Transfer " + transferId + " was not found.");
    }

    @Override
    public List<Transfer> findAll() {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfer;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public List<Transfer> findByUser(long userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfer " +
                "WHERE (account_to = (SELECT account_id FROM account WHERE user_id = ?) OR " +
                "account_from = (SELECT account_id FROM account WHERE user_id = ?));";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
        while(results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public List<Transfer> findInboundPending(long userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfer " +
                "WHERE account_from = (SELECT account_id FROM account WHERE user_id = ?) " +
                "AND transfer_status_id = ? AND transfer_type_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, PENDING_STATUS_ID, INBOUND_TYPE_ID);
        while(results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

    @Override
    public Integer create(int transferType, int accountFrom, int accountTo, BigDecimal amount) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        int newTransferId;
        try {
            newTransferId = jdbcTemplate.queryForObject(sql, Integer.class, transferType, PENDING_STATUS_ID, accountFrom, accountTo, amount);
        } catch (DataAccessException e) {
            return -1;
        }
        return newTransferId;
    }

    @Override
    public Transfer approve(Transfer transfer) {
        String updateTransferSql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        String withdrawFromSenderSql = "UPDATE account SET balance = (balance - ?) WHERE account_id = ?";
        String depositToRecipientSql = "UPDATE account SET balance = (balance + ?) WHERE account_id = ?";
        String updateTransferHolderSql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount FROM transfer WHERE transfer_id = ?;";
            return transactionTemplate.execute(new TransactionCallback<Transfer>() {
                @Override
                public Transfer doInTransaction(TransactionStatus ts) {
                    Transfer transferHolder = transfer;
                    try {
                        jdbcTemplate.update(updateTransferSql, APPROVED_STATUS_ID, transfer.getTransfer_id());
                        jdbcTemplate.update(withdrawFromSenderSql, transfer.getAmount(), transfer.getAccount_from());
                        jdbcTemplate.update(depositToRecipientSql, transfer.getAmount(), transfer.getAccount_to());
                        transferHolder = jdbcTemplate.queryForObject(updateTransferHolderSql, Transfer.class, transfer.getTransfer_id());
                    } catch (Exception e) {
                        ts.isRollbackOnly();
                    }
                    return transferHolder;
                }
            });
    }

    @Override
    public boolean reject(Transfer transfer) {
        String sql ="UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        Integer newTransferId;
        try {
            jdbcTemplate.update(sql, REJECTED_STATUS_ID, transfer.getTransfer_id());
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransfer_id(rs.getInt("transfer_id"));
        transfer.setTransferType(rs.getInt("transfer_type_id"));
        transfer.setTransferStatus(rs.getInt("transfer_status_id"));
        transfer.setAccount_from(rs.getInt("account_from"));
        transfer.setAccount_to(rs.getInt("account_to"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        return transfer;
    }
}
