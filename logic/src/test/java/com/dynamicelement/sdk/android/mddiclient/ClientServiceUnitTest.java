package com.dynamicelement.sdk.android.mddiclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.dynamicelement.sdk.android.TestConstants;
import com.dynamicelement.sdk.android.exceptions.ConfigurationException;
import com.dynamicelement.sdk.android.misc.InstanceType;

import org.junit.Test;

import java.util.Objects;

public class ClientServiceUnitTest {
    String hostIP = TestConstants.hostIP;
    int port = TestConstants.port;
    String username = TestConstants.username;
    String userid = TestConstants.userid;
    String password = TestConstants.password;
    String hostname = TestConstants.hostname;
    InstanceType instanceType = TestConstants.instanceType;
    String cert = TestConstants.cert;

    @Test
    public void ClientService_CheckConfigurationParametersInvalidIpTest() {
        try {
            new ClientService("3.4..4", this.port, this.username, this.password, this.userid, this.hostname,
                    this.cert, this.instanceType, "", true);
        } catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
            assertEquals("Wrong IP format", e.getMessage());
        }
    }

    @Test
    public void ClientService_CheckConfigurationParametersInvalidPortTest() {
        try {
            new ClientService(this.hostIP, 4677, this.username, this.password, this.userid, this.hostname,
                    this.cert, this.instanceType, "", true);
        } catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
            assertEquals("Wrong port", e.getMessage());
        }
    }

    @Test
    public void ClientService_CheckConfigurationParametersInvalidCertificateTest() {
        try {
            new ClientService(this.hostIP, this.port, this.username, this.password, this.userid, this.hostname,
                    "test", this.instanceType, "", true);
        } catch (Exception e) {
            assertTrue(e instanceof ConfigurationException);
            assertTrue(Objects.requireNonNull(e.getMessage()).startsWith("Could not parse"));
        }
    }

//    @Test
//    public void ClientService_StopSearchTest() {
//       ClientService clientService = new ClientService(this.hostIP, this.port, this.username,
//                this.password, this.userid, this.hostname,
//                this.cert, this.instanceType, "", true);
//            clientService.stopMddi();
//            assertFalse(clientService.searchTaskCreated);
//    }
//
//    @Test
//    public void ClientService_StopAddTest() {
//       ClientService clientService = new ClientService(this.hostIP, this.port, this.username,
//                this.password, this.userid, this.hostname,
//                this.cert, this.instanceType, "", true);
//            clientService.stopMddi();
//            assertFalse(clientService.addTaskCreated);
//    }
}