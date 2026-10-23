package com.inventorylite.stock;

public enum MovementType {
    IN,
    OUT;

    int signed(int quantity) {
        return this == IN ? quantity : -quantity;
    }
}
