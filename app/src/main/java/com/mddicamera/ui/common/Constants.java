package com.mddicamera.ui.common;

import static com.dynamicelement.sdk.android.misc.InstanceType.DB_SNO;

import com.dynamicelement.sdk.android.collection.CollectionInfo;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiVariables;

public interface Constants {
    String hostDefaultEC2 = "3.121.52.42";
   // String hostDefaultEC2 = "52.59.118.81";
    int port = 443;
    String username = "sri";
    String userid = "sri9101";
    String password = "9101";
    String hostname = "www.mddiservice.com";
    String tenantId = "dne";
    String cert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDvTCCAqWgAwIBAgIUC4Q+pLuURPmwGPVnV11Hsg8oFA0wDQYJKoZIhvcNAQEL\n" +
            "BQAwbjELMAkGA1UEBhMCREUxEDAOBgNVBAgMB0dlcm1hbnkxEzARBgNVBAcMClJh\n" +
            "dmVuc2J1cmcxDTALBgNVBAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxGjAYBgNVBAMM\n" +
            "ESoubWRkaXNlcnZpY2UuY29tMB4XDTIxMTEwNTE0MDMzNloXDTMxMTEwMzE0MDMz\n" +
            "NlowbjELMAkGA1UEBhMCREUxEDAOBgNVBAgMB0dlcm1hbnkxEzARBgNVBAcMClJh\n" +
            "dmVuc2J1cmcxDTALBgNVBAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxGjAYBgNVBAMM\n" +
            "ESoubWRkaXNlcnZpY2UuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC\n" +
            "AQEAuwP6kwivD5JzinOLzkfHfJmM8Dz45VX/i03ND5fpFo+SK0K7MsvHaPGRJ/YZ\n" +
            "Q8W8hPTPPNDtYq75k9b3emZrUUc14VZsbwFEN228NZDdKnUvyV7XhN0qZmUCv8Vg\n" +
            "wqTnLoXevGIkrBYAHkLcsaNGpQw9AjN7AG2zcvSpJPY3UJYxiH0i51d9MAgYExBT\n" +
            "JteZjTBqpuAeB/rIxOhsg8IUY5TxXKi+XNcSxT4werX5tnn+KT8tur5lOlwyIbL4\n" +
            "x5yg4JuIkcWdibrIdD1uM1lBzukoHnIeJZ4TT7fQZyjgtXEISSA5tzH86L6yYT/F\n" +
            "2dzwCzPEQbyOTo7RjtyCZa7TCQIDAQABo1MwUTAdBgNVHQ4EFgQU9N3vO75CKvSM\n" +
            "UvHW58z1aNm/y9owHwYDVR0jBBgwFoAU9N3vO75CKvSMUvHW58z1aNm/y9owDwYD\n" +
            "VR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAQEAsvzToMLGSz30MFsf1loB\n" +
            "hLmsbaUqKhYEZoSVj9/Q1aSrMdB3kg1Qvb7E/8Omov2bectw2N6eF3DhbLPU1dDM\n" +
            "+WPULU1fnq3wyh9cDUJMp+f17xQjSl6Cfx07K6efBYhM1I4dmzZKVAYxA2TMWzIJ\n" +
            "rkzf2QU6CTRQK3nen4qVB1xMe5ByqzPy0Y+dNzji7r0YUaMIksjQCp3XVMtzI8Pq\n" +
            "pLupWhIUmVb7zKvjMbs9pQ2oNBZYAx4CvMw76ycHISnkJA0+BM9UKT2VpFSIpMFh\n" +
            "U/vIuD9zanbOsOlP6rDhq78PHhXkD15FqlrjKIN1mTj+/Gi2/8g/XbsMt8Bt56kD\n" +
            "yA==\n" +
            "-----END CERTIFICATE-----";
    boolean DEFAULT_TOGGLE_FLASH = true;
    boolean DEFAULT_SHOW_SCORE = true;
    boolean DEFAULT_OVERLAY = false;
    boolean DEFAULT_ZOOM_BUTTON_VISIBLE = false;
    String DEFAULT_CID = "sridhar01";
    String DEFAULT_SNO = "sri01";
    int NEGATIVE_SEARCH_THRESHOLD = 15;
    int DEFAULT_LOGO_PRESS_COUNT = 5;
    int VIBRATION_MS = 60;


    static CollectionInfo getCollectionInfo() {
        CollectionInfo collectionInfo = new CollectionInfo();
        collectionInfo.setName("PLI");
        collectionInfo.setVersionId("V1.0");
        collectionInfo.setOwnerId("post1234");
        collectionInfo.setLongDescription("test");
        collectionInfo.setShortDescription("test");
        return collectionInfo;
    }

    /**
     * Get the default EC2 1:1 client service
     */
    static ClientService getDefaultEc2ClientService1_1() {
        return new ClientService(Constants.hostDefaultEC2, port, username, password, userid,
                hostname, cert, DB_SNO, tenantId, true).
                updateMddiImageSize(MddiVariables.MddiImageSize.FORMAT_512X512);
    }
}
