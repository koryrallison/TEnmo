package com.techelevator.tenmo.enums;

import java.math.BigDecimal;

public enum TransferStatus {
    PENDING_STATUS_ID(1, "Pending"),
    APPROVED_STATUS_ID(2, "Approved"),
    REJECTED_STATUS_ID(3, "Rejected"),
    INVALID_STATUS_ID(4, "Invalid"),
    COMPLETED_STATUS_ID(5, "Completed"),
    FAILED_STATUS_ID(6, "Failed");

    private final int status_id;
    private final String status_message;

    TransferStatus(int statusId, String statusMessage){
        this.status_id = statusId;
        this.status_message = statusMessage;
    }

    public static String getStatusById(int i) {
        for (TransferStatus status : TransferStatus.values()) {
            if (status.getStatus_id() == i) {return status.status_message;}
        }
        return null;
    }

    public int getStatus_id() {
        return status_id;
    }

    public String getStatus_message() {
        return status_message;
    }
}
