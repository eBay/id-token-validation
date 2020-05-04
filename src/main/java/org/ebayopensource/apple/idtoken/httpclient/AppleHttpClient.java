package org.ebayopensource.apple.idtoken.httpclient;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.ebayopensource.apple.idtoken.ApplePublicKeysManager;

import java.io.IOException;

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

public class AppleHttpClient {

    private static final String APPLE_GET_PUBLIC_KEY_ENDPOINT = "https://appleid.apple.com/auth/keys";

    private static final AppleHttpClient appleHttpClient = new AppleHttpClient();

    private AppleHttpClient() {}

    public static AppleHttpClient getAppleHttpClient() {
        return appleHttpClient;
    }

    /**
     * Function that returns Apple's public key.
     *
     * @return Apple's public key in String
     * @throws IOException
     */
    public String fetchApplePublicKeyResponse() throws IOException {

        ApplePublicKeysManager manager = ApplePublicKeysManager.getApplePublicKeysManager();
        String response = null;
        HttpClient client = new HttpClient();
        HttpMethod getMethod = new GetMethod(APPLE_GET_PUBLIC_KEY_ENDPOINT);

        if(manager.isProxyEnabled()) {
            HostConfiguration hostConfiguration = client.getHostConfiguration();
            hostConfiguration.setProxy(manager.getProxyHost(), manager.getProxyPort());
            client.setHostConfiguration(hostConfiguration);
        }

        client.executeMethod(getMethod);
        if(getMethod.getStatusCode() == HttpStatus.SC_OK) {
            response = getMethod.getResponseBodyAsString();
        }

        return response;
    }
}
