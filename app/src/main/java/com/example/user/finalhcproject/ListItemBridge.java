package com.example.user.finalhcproject;

// The listItemBridge class is used for bridge property operations

public class ListItemBridge {

    private String bridgeName;
    private String bridgeIP;
    private String bridgeID;
    private String bridgeModel;

    // Method to set the values of the bridge object
    public ListItemBridge(String name, String id, String ip, String model) {
        this.bridgeName = name;
        this.bridgeID = id;
        this.bridgeIP = ip;
        this.bridgeModel = model;
    }

    // Method to return the model of the bridge
    public String getBridgeModel() {
        return bridgeModel;
    }

    // Method to return the name of the bridge
    public String getBridgeName() {
        return bridgeName;
    }

    // Method to return the IP address of the bridge
    public String getBridgeIP() {
        return bridgeIP;
    }

    // Method to return the ID of the bridge
    public String getBridgeID() {
        return bridgeID;
    }
}
