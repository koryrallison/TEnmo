package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    Transfer findById(long transferId);

    List<Transfer> findAll();

    List<Transfer> findByUser(long userId);

    List<Transfer> findInboundPending(long userId);

    Integer create(Transfer transfer);

    Transfer approve(Transfer transfer);

    Transfer reject(Transfer transfer);

    Transfer invalidate(Transfer transfer);
}
