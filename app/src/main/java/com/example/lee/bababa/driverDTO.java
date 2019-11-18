package com.example.lee.bababa;

public class driverDTO {
    public String driverId;
    public String driverName;
    public String busNumber;
    public String busCarNumber;

    driverDTO() {

    }

    driverDTO (String driverId, String driverName, String busNumber, String busCarNumber) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.busNumber = busNumber;
        this.busCarNumber = busCarNumber;
    }
}
