package com.DyncoApp.sqlitedb;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CredentialsData {
    String host1;
    String port1;
    String username1;
    String userid1;
    String password1;
    String instanceType1;
    String currentInstance;
    boolean sqlEnable1;
    String host2;
    String port2;
    String username2;
    String password2;
    String database2;
    String table2;

    @Builder
    public CredentialsData(String host1, String port1, String username1, String userid1, String password1,
                           String instanceType, String currentInstance, boolean sqlEnable1, String host2, String port2,
                           String username2, String password2, String database2, String table2) {
        this.host1 = host1;
        this.port1 = port1;
        this.username1 = username1;
        this.userid1 = userid1;
        this.password1 = password1;
        this.currentInstance = currentInstance;
        this.instanceType1 = instanceType;
        this.sqlEnable1 = sqlEnable1;
        this.host2 = host2;
        this.port2 = port2;
        this.username2 = username2;
        this.password2 = password2;
        this.database2 = database2;
        this.table2 = table2;
    }
}
