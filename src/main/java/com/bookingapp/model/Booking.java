package com.bookingapp.model;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Booking {
    private String customerName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private int tableSize;

    // Constructor, getters, and toString method

    public Booking(String customerName,  int tableSize, LocalDateTime startDateTime) {
        this.customerName = customerName;
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusHours(2);
        this.tableSize = tableSize;
    }

    public String getCustomerName() {
        return customerName;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }


    public int getTableSize() {
        return tableSize;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "customerName='" + customerName + '\'' +
                ", startDateTime='" + startDateTime + '\'' +
                ", endDateTime='" + endDateTime + '\'' +
                ", tableSize=" + tableSize +
                '}';
    }

    public JSONObject toJson() {
        HashMap<String, String> map = new HashMap<>();
        map.put("customerName", customerName);
        map.put("tableSize", Integer.valueOf(tableSize).toString());
        map.put("startDateTime", startDateTime.toString());
        map.put("endDateTime", endDateTime.toString());
        return new JSONObject(map);
    }
}