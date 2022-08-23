package com.DyncoApp.ui;

import static com.mddi.misc.InstanceType.DB_SNO;
import static com.mddi.misc.InstanceType.IVF;
import static com.mddi.misc.InstanceType.IVF_SNO;

import com.mddi.exceptions.ConfigurationException;
import com.mddi.mddiclient.ClientService;
import com.mddiv1.misc.InstanceType;

public interface Constants {
    String hostIP_IvfSno = "3.68.148.143";
    String hostIP_DbSnoPro = "18.193.44.245";
    String hostIP_DbSnoInternal = "3.65.251.79";
    String hostIP_IvfPro = "18.159.47.218";
    String hostIP_IvfInternal = "18.193.133.33";
    String hostIP_directoryService = "3.64.43.12";
    int port = 443;
    String username = "sri";
    String userid = "sri9101";
    String password = "9101";
    String hostname = "www.mddiservice.com";
    com.mddi.misc.InstanceType instanceType = IVF_SNO;
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

    String ivfProCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFsTCCA5mgAwIBAgIUQoknGB6jz0t/aL3Bd0uQvLGOGQUwDQYJKoZIhvcNAQEL\n" +
            "BQAwaDELMAkGA1UEBhMCREUxEDAOBgNVBAgMB0dlcm1hbnkxEzARBgNVBAcMClJh\n" +
            "dmVuc2J1cmcxDTALBgNVBAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxFDASBgNVBAMM\n" +
            "C21kZGlzZXJ2aWNlMB4XDTIxMDcxNjAzMzE0NloXDTI0MDcxNTAzMzE0NlowaDEL\n" +
            "MAkGA1UEBhMCREUxEDAOBgNVBAgMB0dlcm1hbnkxEzARBgNVBAcMClJhdmVuc2J1\n" +
            "cmcxDTALBgNVBAoMBFRlc3QxDTALBgNVBAsMBFRlc3QxFDASBgNVBAMMC21kZGlz\n" +
            "ZXJ2aWNlMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAw0WKhGcqG67N\n" +
            "vN0pnBTheWVybrT1JyCgfwZ0i8V0cPBVZWi9FoY43cbTEhgVZFW7L0wwfbDhGlaj\n" +
            "QsrKFDJgT++5dDciWzI3o3fNSZKV7vs8Gvqm9y1GDw7ML0Ltyo+HRQZIsrE8fTn+\n" +
            "nKKKFpQhwiig4g6uer7+rJ6ivr8e99sUUxnYzvP9pY5OjSoJRUl+thwAWW5bDfXd\n" +
            "YhGA2OykHRJjYpOllcsh1NRWd/nAHZSMkdYybVlT1ImtXRVvc3y4NFoo0BBiiiVG\n" +
            "omCFu3RBeAPb1CyeG1xq3zGdvBgs/RgsXQSqH/ds3UnyIK8tVcgeaSxmT5RcA4Vs\n" +
            "aNHIY62eDYQAKWG2Q4MpfaQpWFKkYuF1HoC1ei0+UND6RGXIVavXukJiNVgkXHQe\n" +
            "J1uVqObMezCzkWM9izzsLwK8Eucw6xpIYdkBaenvlg8OtHsd4I1mW2Rz139zMJ41\n" +
            "WpozQ1iI2BiLnz6zSCnfFEfEj5m5lYcH/N1/2FTdk1YiIrnz1S3jwfiJxWVPBRbu\n" +
            "yOcGZAPznMAFjGjHJ1GtlEFnDOjYznHR6gE4x5630vevaRRwlgFVkPZUARVXyrlU\n" +
            "xqg5/PoDiKMO67vgdZzSxtRUfl9kIxLpVVC8Hw2H1IFh5PawtiwnU3fM86JaeDK9\n" +
            "HEKLxdueMvtSKEOGOQxiVvBWolwufKECAwEAAaNTMFEwHQYDVR0OBBYEFHDcQdk0\n" +
            "fPcYf6pPJsAOCA2M75iBMB8GA1UdIwQYMBaAFHDcQdk0fPcYf6pPJsAOCA2M75iB\n" +
            "MA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggIBABEHdvh3x6VqSWYb\n" +
            "v254SUIQ7N6l+fOQ+626wO8HwHcxqo0o8bAE6C+y7jC28JtLEewJxvEz68Aroycu\n" +
            "9qr6FEgm/phKDwWLhEi8U1d6sKqAsaq1mynPTW9C0TLrXuEQd4ZsFqAYVmB9IZuT\n" +
            "j0TP65zn4+aZ29Q3UYOp1tW3+gOu017INM53id0uv7sxO+aslmYFOMkVavqJodce\n" +
            "o2wEIXIsI4po012V6jvYS3vpNHuzoioMxBBZc11oQXDoKYSsvyMV31QJXHF+yyu0\n" +
            "VO5IJnxzmwUoFoaKRulftrtvXs44+RS1BQXVvhZaFNo0iiCZdJKRvOYH18Z4d8IF\n" +
            "pF1UF/SdWbMXbbXEsP8y+r44K+9pAqwCB8MyO6J1OkdkZJygSwV8XhWzHYjk1ZQY\n" +
            "3fTnzHbHAAoji6oi4b9jgaVb9ijl26OLUsvX38Zp2LIti3xInCniJL9JRJIELWCt\n" +
            "DAKI740HG3sFXl3+ZibxzqAOsgOqAcycRx7AeWXXZXXYvKuLnIZI7PyzIq8vwrVC\n" +
            "tF7/RYleiV/J037a+gWn5F0fiezfW/T1zeVvDw0Z/+hf8kVpkdV5Tqh+YRjDQHPQ\n" +
            "LO90gNUlqL/keetxr3Lo07p0pKYsRYoVZDYfdHYtO+1rHLvZXYMwtHn6/g21ORxl\n" +
            "stkh4E8j2iPM6dqVQHVu/CWrALTx\n" +
            "-----END CERTIFICATE-----\n";

    /**
     * Get the ivf sno client service
     */
    static ClientService getIvfSnoClientService() {
        return ClientService.builder().host(Constants.hostIP_IvfSno).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                caCert(Constants.cert).InstanceType(Constants.instanceType).build().createNewSession();
    }

    /**
     * Get the db sno pro client service
     */
    static ClientService getDbSnoProClientService() {
        return ClientService.builder().host(Constants.hostIP_DbSnoPro).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                caCert(Constants.cert).InstanceType(DB_SNO).build().createNewSession();
    }

    /**
     * Get the ivf pro client service
     */
    static ClientService getIvfProClientService() {
        return ClientService.builder().host(Constants.hostIP_IvfPro).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName("mddiservice").
                caCert(Constants.ivfProCert).InstanceType(IVF).build().createNewSession();
    }

    /**
     * Get the ivf internal client service
     */
    static ClientService getIvfInternalClientService() {
        return ClientService.builder().host(Constants.hostIP_IvfInternal).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                caCert(Constants.cert).InstanceType(IVF).build().createNewSession();
    }

    /**
     * Get the db sno internal client service
     */
    static com.mddiv1.mddiclient.ClientService getDbSnoInternalClientService() {
        return com.mddiv1.mddiclient.ClientService.builder().host(Constants.hostIP_DbSnoInternal).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                certID(Constants.cert).InstanceType(InstanceType.DB_SNO).build().createNewSession();
    }

    /**
     * Get the new ivf internal client service
     */
    static com.mddiv1.mddiclient.ClientService getNewIVFInternalClientService() {
        return com.mddiv1.mddiclient.ClientService.builder().host(Constants.hostIP_directoryService).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                certID(Constants.cert).InstanceType(InstanceType.DB_SNO).build().createNewSession();
    }

    /**
     * Get the directory client service
     */
    static com.mddiv1.mddiclient.ClientService getDirectoryClientService() {
        return com.mddiv1.mddiclient.ClientService.builder().host(Constants.hostIP_directoryService).port(Constants.port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                certID(Constants.cert).InstanceType(InstanceType.DB_SNO).build().createNewSession();
    }

    /**
     * Get the Mddi backend service
     */
    static com.mddiv1.mddiclient.ClientService getMddiBackendService(String host,int port,InstanceType instanceType) throws ConfigurationException {
        return com.mddiv1.mddiclient.ClientService.builder().host(host).port(port).
                userName(Constants.username).password(Constants.password).userID(Constants.userid).hostName(Constants.hostname).
                certID(Constants.cert).InstanceType(instanceType).build().createNewSession();
    }
}
