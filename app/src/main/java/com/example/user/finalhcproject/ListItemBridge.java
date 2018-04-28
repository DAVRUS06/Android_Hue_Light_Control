package com.example.user.finalhcproject;


public class ListItemBridge {

    private String bridgeName;
    private String bridgeIP;
    private String bridgeID;
    private String bridgeModel;

    public ListItemBridge(String name, String id, String ip, String model) {
        this.bridgeName = name;
        this.bridgeID = id;
        this.bridgeIP = ip;
        this.bridgeModel = model;
    }

    public String getBridgeModel() {
        return bridgeModel;
    }

    public String getBridgeName() {
        return bridgeName;
    }

    public String getBridgeIP() {
        return bridgeIP;
    }

    public String getBridgeID() {
        return bridgeID;
    }
}
