package com.techelevator.tenmo.enums;

public enum TransferType {
    REQUEST_TYPE_ID(1, "Request"),
    SEND_TYPE_ID(2, "Send");

    private final int type_id;
    private final String type_message;

    TransferType(int typeId, String typeMessage){
        this.type_id = typeId;
        this.type_message = typeMessage;
    }

    public static String getTypeById(int i) {
        for (TransferType type : TransferType.values()) {
            if (type.getType_id() == i) {return type.type_message;}
        }
        return null;
    }

    public int getType_id() {
        return type_id;
    }

    public String getType_message() {
        return type_message;
    }
}
