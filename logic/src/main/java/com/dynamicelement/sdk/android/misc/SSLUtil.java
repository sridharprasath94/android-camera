package com.dynamicelement.sdk.android.misc;

import com.dynamicelement.sdk.android.exceptions.ConfigurationException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtil {
    /**
     * Create SSL factory for the TLS connection
     *
     * @param caCertInStringFormat is the 'CA' certificate in string format.
     * @return SSLSocketFactory
     * @throws ConfigurationException
     */
    public static SSLSocketFactory createSslFactoryContext(String caCertInStringFormat) throws ConfigurationException {
        try {
            KeyStore caKeystore;
            Certificate caCertificate;
            TrustManagerFactory caTrustManager;
            InputStream caCertificateStream;

            caCertificateStream = new ByteArrayInputStream(caCertInStringFormat.getBytes());
            try (InputStream inputStreamAutoClosable = caCertificateStream) {
                //////////////////////////////////////Adding the CA certificate to the
                // TrustManager//////////////////////////////////////
                caCertificate =
                        CertificateFactory.getInstance("X.509").generateCertificate(inputStreamAutoClosable);
            }
            //Assigning the CA certificate to the keystore
            caKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            caKeystore.load(null, null);
            caKeystore.setCertificateEntry("ca_crt", caCertificate);
            //Initialising the TrustManagerFactory with the CA_keystore
            caTrustManager =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            caTrustManager.init(caKeystore);
            System.out.println("CA =" + ((X509Certificate) caCertificate).getSubjectDN());
            //////////////////////////////////////Adding TrustManager and KeyManager to
            // SSL//////////////////////////////////////
            // Initialize SSL context with the trust manager factory and the KeyManager factory
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, caTrustManager.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }
}
