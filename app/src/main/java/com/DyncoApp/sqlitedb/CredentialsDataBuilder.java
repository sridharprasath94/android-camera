package com.DyncoApp.sqlitedb;

public class CredentialsDataBuilder {
    private String host1;
    private String port1;
    private String username1;
    private String userid1;
    private String password1;
    private String instanceType;
    private String currentInstance;
    private boolean sqlEnable1;
    private String host2;
    private String port2;
    private String username2;
    private String password2;
    private String database2;
    private String table2;

    public CredentialsDataBuilder setHost1(String host1) {
        this.host1 = host1;
        return this;
    }

    public CredentialsDataBuilder setPort1(String port1) {
        this.port1 = port1;
        return this;
    }

    public CredentialsDataBuilder setUsername1(String username1) {
        this.username1 = username1;
        return this;
    }

    public CredentialsDataBuilder setUserid1(String userid1) {
        this.userid1 = userid1;
        return this;
    }

    public CredentialsDataBuilder setPassword1(String password1) {
        this.password1 = password1;
        return this;
    }

    public CredentialsDataBuilder setInstanceType(String instanceType) {
        this.instanceType = instanceType;
        return this;
    }

    public CredentialsDataBuilder setCurrentInstance(String currentInstance) {
        this.currentInstance = currentInstance;
        return this;
    }

    public CredentialsDataBuilder setSqlEnable1(boolean sqlEnable1) {
        this.sqlEnable1 = sqlEnable1;
        return this;
    }

    public CredentialsDataBuilder setHost2(String host2) {
        this.host2 = host2;
        return this;
    }

    public CredentialsDataBuilder setPort2(String port2) {
        this.port2 = port2;
        return this;
    }

    public CredentialsDataBuilder setUsername2(String username2) {
        this.username2 = username2;
        return this;
    }

    public CredentialsDataBuilder setPassword2(String password2) {
        this.password2 = password2;
        return this;
    }

    public CredentialsDataBuilder setDatabase2(String database2) {
        this.database2 = database2;
        return this;
    }

    public CredentialsDataBuilder setTable2(String table2) {
        this.table2 = table2;
        return this;
    }

    public CredentialsData createCredentialsData() {
        return new CredentialsData(host1, port1, username1, userid1, password1, instanceType, currentInstance, sqlEnable1, host2, port2, username2, password2, database2, table2);
    }
}