package com.bookingapp.dto;

import com.bookingapp.model.Booking;

import java.time.LocalDateTime;

public class BookingRequest {
    private final String customerName;
    private final int tableSize;
    private final LocalDateTime dateTime;

    public BookingRequest(String customerName, int tableSize, LocalDateTime dateTime) {
        this.customerName = customerName;
        this.tableSize = tableSize;
        this.dateTime = dateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getTableSize() {
        return tableSize;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


}