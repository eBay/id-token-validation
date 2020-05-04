package org.ebayopensource.apple.idtoken;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.StringUtils;
import org.ebayopensource.apple.idtoken.entities.AppleJWKSet;
import org.ebayopensource.apple.idtoken.entities.ApplePublicKey;
import org.ebayopensource.apple.idtoken.httpclient.AppleHttpClient;
import org.ebayopensource.apple.idtoken.util.IDTokenErrorEnum;
import org.ebayopensource.apple.idtoken.util.IDTokenException;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/************************************************************************
 Copyright 2020 eBay Inc.
 Author/Developer(s): Chetan Hibare; Dhairyasheel Desai; Swanand Abhyankar

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **************************************************************************/

public class ApplePublicKeysManager {

    /** Number of milliseconds before expiration time to force a refresh. */
    private long refreshSkewMilliseconds = 86400000;

    /** Singleton object. */
    private static final ApplePublicKeysManager applePublicKeysManager = new ApplePublicKeysManager();

    private List<PublicKey> applePublicKeys;

    /** Expiration time in milliseconds to refresh fetching of public key */
    private long expirationTimeInMillis;

    /** Indicates whether Proxy is enabled or not. */
    private boolean isProxyEnabled = false;

    /** Proxy host to be set by consumer, if needed. */
    private String proxyHost;

    /** Proxy port to be set by consumer, if needed. */
    private int proxyPort;

    /** Lock on the public keys. */
    private final Lock lock = new ReentrantLock();

    private AppleHttpClient appleHttpClient = AppleHttpClient.getAppleHttpClient();

    /** Singleton constructor. */
    public static ApplePublicKeysManager getApplePublicKeysManager() {
        return applePublicKeysManager;
    }

    private ApplePublicKeysManager() {}

    /** Returns whether proxy is enabled or not. */
    public boolean isProxyEnabled() {
        return isProxyEnabled;
    }

    
    public void setProxyEnabled(boolean isProxyEnabled) {
		this.isProxyEnabled = isProxyEnabled;
	}

	/** Returns whether proxy host. */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Set the proxy host used while connecting to Apple API for fetching public keys
     * @param proxyHost proxy host in String
     * @return ApplePublicKeysManager object after setting proxy host
     * @throws IDTokenException
     */
    public ApplePublicKeysManager setProxyHost(String proxyHost) throws IDTokenException {
        if(!StringUtils.isEmpty(proxyHost)) {
            this.proxyHost = proxyHost;
            this.isProxyEnabled = true;
            return this;
        } else {
            throw new IDTokenException(IDTokenErrorEnum.PROXY_SETUP_ERROR);
        }

    }

    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Set the proxy port used while connecting to Apple API for fetching public keys
     * @param proxyPort proxy host in int
     * @return ApplePublicKeysManager object after setting proxy port
     * @throws IDTokenException
     */
    public ApplePublicKeysManager setProxyPort(int proxyPort) throws IDTokenException {
        if(proxyPort > 0) {
            this.proxyPort = proxyPort;
            this.isProxyEnabled = true;
            return this;
        } else {
            throw new IDTokenException(IDTokenErrorEnum.PROXY_SETUP_ERROR);
        }

    }

    public long getRefreshSkewMilliseconds() {
        return refreshSkewMilliseconds;
    }

    public ApplePublicKeysManager setRefreshSkewMilliseconds(long refreshSkewMilliseconds) {
        this.refreshSkewMilliseconds = refreshSkewMilliseconds;
        return this;
    }

    /**
     * Function that returns list of Apple PublicKey objects.
     * Existing initialized keys would be returned if 
     *
     * @return
     * @throws IDTokenException
     */
    public final List<PublicKey> getApplePublicKeys() throws IDTokenException {
        lock.lock();
        try {
            if(applePublicKeys == null || System.currentTimeMillis() + refreshSkewMilliseconds > expirationTimeInMillis) {
                refreshApplePublicKeys();
            }
            return applePublicKeys;
        } finally {
            lock.unlock();
        }
    }

    public ApplePublicKeysManager refreshApplePublicKeys() throws IDTokenException {
        lock.lock();
        try {

            applePublicKeys = new ArrayList<PublicKey>();
            AppleJWKSet keys = fetchRawPublicKeys();

            if(keys == null || CollectionUtils.isEmpty(keys.getKeys())){
                throw new IDTokenException(IDTokenErrorEnum.APPLE_PUBLIC_KEY_UNAVAILABLE);
            }

            String modulus, exponent;
            BigInteger modulusAsBigInt, exponentAsBigInt;
            KeyFactory factory = KeyFactory.getInstance("RSA");

            for(ApplePublicKey applePublicKey: keys.getKeys()) {

                modulus = applePublicKey.getModulus();
                exponent = applePublicKey.getExponent();

                modulusAsBigInt = new BigInteger(1, Base64.decodeBase64(modulus));
                exponentAsBigInt = new BigInteger(1, Base64.decodeBase64(exponent));

                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulusAsBigInt, exponentAsBigInt);
                applePublicKeys.add(factory.generatePublic(spec));
            }
            applePublicKeys = Collections.unmodifiableList(applePublicKeys);
            expirationTimeInMillis = System.currentTimeMillis() + refreshSkewMilliseconds;
            return this;

        } catch (NoSuchAlgorithmException e) {
            throw new IDTokenException(IDTokenErrorEnum.APPLE_SIGNIN_PUBLIC_KEY_ERROR);
        } catch (InvalidKeySpecException e) {
            throw new IDTokenException(IDTokenErrorEnum.APPLE_SIGNIN_PUBLIC_KEY_ERROR);
        } catch (JsonSyntaxException e) {
            throw new IDTokenException(IDTokenErrorEnum.APPLE_PUBLIC_KEY_UNAVAILABLE);
        } catch (IDTokenException e) {
            throw new IDTokenException(IDTokenErrorEnum.APPLE_PUBLIC_KEY_UNAVAILABLE);
        } catch (Exception e) {
            throw new IDTokenException(IDTokenErrorEnum.APPLE_SIGNIN_PUBLIC_KEY_ERROR, e);
        } finally {
            lock.unlock();
        }
    }

    private AppleJWKSet fetchRawPublicKeys() throws Exception {
        try {
            String response = appleHttpClient.fetchApplePublicKeyResponse();
            if (!StringUtils.isEmpty(response)) {
                return new Gson().fromJson(response, AppleJWKSet.class);
            }
        } catch (Exception e) {
            throw new IDTokenException(IDTokenErrorEnum.APPLE_PUBLIC_KEY_UNAVAILABLE);
        }

       throw new IDTokenException(IDTokenErrorEnum.APPLE_PUBLIC_KEY_UNAVAILABLE);

    }
}
