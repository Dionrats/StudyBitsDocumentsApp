package com.example.studybitsdocuments.models;

public class DidModel {
    private String did;
    private String verkey;
    private String tempVerkey;
    private String metadata;

    public DidModel(String did, String verkey, String tempVerkey, String metadata) {
        this.did = did;
        this.verkey = verkey;
        this.tempVerkey = tempVerkey;
        this.metadata = metadata;
    }

    public DidModel() {}

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getVerkey() {
        return verkey;
    }

    public void setVerkey(String verkey) {
        this.verkey = verkey;
    }

    public String getTempVerkey() {
        return tempVerkey;
    }

    public void setTempVerkey(String tempVerkey) {
        this.tempVerkey = tempVerkey;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}


