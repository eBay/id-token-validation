package org.ebayopensource.apple.idtoken.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.security.*;
import java.util.HashMap;
import java.util.Map;

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

public class TestUtils {

    private static String defaultId = "come.ebay.apple";
    private static String defaultIssuer = "https://appleid.apple.com";
    private static String defaultSubject = "someSubject";
    private static String defaultEmail = "abcd@gmail.com";
    private static String defaultNonce = "a1b2c3d4";
    private static String fakeSignature = "thisisafakesignature";

    public static String getDefaultIssuer() {
        return defaultIssuer;
    }

    public static String getDefaultEmail() {
        return defaultEmail;
    }

    public static String getDefaultNonce() {
        return defaultNonce;
    }

    public static String createValidJWTWithFakeSignature(String issuer, String aud, String nonce, long ttlMillis) {

        if(StringUtils.isEmpty(issuer)) {
            issuer = defaultIssuer;
        }

        if(StringUtils.isEmpty(aud)){
            aud = defaultId;
        }

        if(StringUtils.isEmpty(nonce)){
            nonce = defaultNonce;
        }

        Map<String, Object> headerParams = new HashMap<>();
        headerParams.put("kid", "AIDOPK1");
        headerParams.put("alg", "RS256");

        JwtBuilder builder = Jwts.builder()
                .setHeader(headerParams)
                .setClaims(null)
                .setPayload(generatePayload(issuer, aud, nonce, ttlMillis));


        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact()+fakeSignature;
    }

    public static String createValidJWTWithNoSignature(String issuer, String aud, String nonce, long ttlMillis) {

        if(StringUtils.isEmpty(issuer)) {
            issuer = defaultIssuer;
        }

        if(StringUtils.isEmpty(aud)){
            aud = defaultId;
        }

        if(StringUtils.isEmpty(nonce)){
            nonce = defaultNonce;
        }

        Map<String, Object> headerParams = new HashMap<>();
        headerParams.put("kid", "AIDOPK1");
        headerParams.put("alg", "RS256");

        JwtBuilder builder = Jwts.builder()
                .setHeader(headerParams)
                .setClaims(null)
                .setPayload(generatePayload(issuer, aud, nonce, ttlMillis));


        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    private static String generatePayload(String iss, String clientId, String nonce, long time) {
        return "{\n" +
                "  \"iss\": \""+iss+"\",\n" +
                "  \"aud\": \""+clientId+"\",\n" +
                "  \"exp\": "+time+",\n" +
                "  \"iat\": 1574380475,\n" +
                "  \"sub\": \"002454.3bffcb36c6a948a5010083b8da5f5f31.1123\",\n" +
                "  \"c_hash\": \"mVFpZwapUaHodbLT5vZ_hg\",\n" +
                "  \"email\": \""+defaultEmail+"\",\n" +
                "  \"email_verified\": \"true\",\n" +
                "  \"auth_time\": 1574380475,\n" +
                "  \"nonce\": \""+nonce+"\"\n" +
                "}";
    }

    public static String createValidJWTWithWrongSignature(String issuer, String aud, String nonce, long ttlMillis) {

        if(StringUtils.isEmpty(issuer)) {
            issuer = defaultIssuer;
        }

        if(StringUtils.isEmpty(aud)){
            aud = defaultId;
        }

        if(StringUtils.isEmpty(nonce)){
            nonce = defaultNonce;
        }

        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        Map<String, Object> headerParams = new HashMap<>();
        headerParams.put("kid", "AIDOPK1");
        headerParams.put("alg", "RS256");

        JwtBuilder builder = Jwts.builder()
                .setHeader(headerParams)
                .setClaims(null)
                .setPayload(generatePayload(issuer, aud, nonce, ttlMillis))
                .signWith(SignatureAlgorithm.RS256, privateKey);


        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }
}
