package org.ebayopensource.apple.idtoken.httpclient;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.ebayopensource.apple.idtoken.entities.AppleJWKSet;
import org.ebayopensource.apple.idtoken.entities.ApplePublicKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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

public class AppleHttpClientTest {

    @Test
    public void testFetchApplePublicKeyResponse() {
        try {
            AppleHttpClient httpClient = AppleHttpClient.getAppleHttpClient();
            String response = httpClient.fetchApplePublicKeyResponse();
            Assert.assertNotNull(response);

            AppleJWKSet appleJWKSet = null;
            try {
                appleJWKSet = new Gson().fromJson(response, AppleJWKSet.class);
            } catch (JsonSyntaxException e){
                Assert.fail("JSON parsing exception while converting Apple's public key response");
            }

            if(appleJWKSet == null) {
                Assert.fail("AppleJWKSet object is null after JSON conversion");
            }
            Assert.assertNotNull(appleJWKSet.getKeys());
            Assert.assertNotNull(appleJWKSet.getKeys().get(0));
            Assert.assertTrue(appleJWKSet.getKeys().size() > 0);

            List<ApplePublicKey> publicKeyList = appleJWKSet.getKeys();

            for(ApplePublicKey applePublicKey: publicKeyList) {
                Assert.assertNotNull(applePublicKey.getExponent());
                Assert.assertNotNull(applePublicKey.getModulus());
                Assert.assertNotNull(applePublicKey.getAlg());
                Assert.assertNotNull(applePublicKey.getKid());
                Assert.assertNotNull(applePublicKey.getKty());
                Assert.assertNotNull(applePublicKey.getUse());
                Assert.assertEquals("RSA",applePublicKey.getKty());
            }

        } catch (Exception e) {
            Assert.fail("Exception while fetching Apple public key response: "+ e.getMessage());
        }
    }

}