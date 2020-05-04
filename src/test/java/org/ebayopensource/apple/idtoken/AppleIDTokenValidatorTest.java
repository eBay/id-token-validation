package org.ebayopensource.apple.idtoken;

import org.ebayopensource.apple.idtoken.entities.AppleUserInfo;
import org.ebayopensource.apple.idtoken.util.IDTokenErrorEnum;
import org.ebayopensource.apple.idtoken.util.IDTokenException;
import org.ebayopensource.apple.idtoken.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
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

public class AppleIDTokenValidatorTest {

    private AppleIDTokenValidator appleIDTokenValidator = new AppleIDTokenValidator();

    @Test
    public void testVerifyAppleIDTokenNullIDToken() {
        boolean result = false;
        try {
            result = appleIDTokenValidator.verifyAppleIDToken(null, null, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenEmptyIDToken() {
        boolean result = false;
        try {
            result = appleIDTokenValidator.verifyAppleIDToken("\n", null, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenInvalidWithNoSignature() {
        boolean result = false;
        try {
            result = appleIDTokenValidator.verifyAppleIDToken("abcd.abcd", null, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenWithMissingSignature() {
        boolean result = false;
        try {
            result = appleIDTokenValidator.verifyAppleIDToken("a.a.", null, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenWithEmptyPayload() {
        boolean result = false;
        try {
            result = appleIDTokenValidator.verifyAppleIDToken("abcd. .adefg", null, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenWithInvalidIssuerInPayload() {
        String idToken = TestUtils.createValidJWTWithFakeSignature("appleid.google.com", null, null, System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.xyzCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken, clientIds, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenWithInvalidIssuerInPayloadSkipCheck() {
        String idToken = TestUtils.createValidJWTWithFakeSignature("appleid.google.com", null, null, System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            result = appleIDTokenValidator.verifyAppleIDToken(idToken, null, null);
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenInvalidClientIdInPayload() {
        String idToken = TestUtils.createValidJWTWithFakeSignature(null, "com.abcCompany.webapp", null, System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.xyzCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken, clientIds, null);
        } catch (IDTokenException e) {
            Assert.fail("IDTokenException exception encountered: "+e.getMessage());
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenEmptyNonceInPayload() {
        String idToken = TestUtils.createValidJWTWithFakeSignature(null, "com.abcCompany.webapp", null, System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.abcCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken, clientIds, null);
            Assert.fail("Expected IDTokenException but no exception encountered.");
        } catch (IDTokenException e) {
            Assert.assertNotNull(e.getErrorEnum());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN, e.getErrorEnum());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN.getErrorId(), e.getErrorEnum().getErrorId());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN.getErrorMsg(), e.getErrorEnum().getErrorMsg());
        }
    }

    @Test
    public void testVerifyAppleIDTokenInvalidNonceInPayload() {
        String idToken = TestUtils.createValidJWTWithFakeSignature(null, "com.abcCompany.webapp", "1234", System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.abcCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken, clientIds, "abcd");
        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyAppleIDTokenInvalidJSONPayloadExpectingException() {
        String idToken = "eyJhbGciOiJub25lIn0.ewogICJpc3MiOiAiaHR0cHM6Ly9hcHBsZWlkLmFwcGxlLmNvbSIsCiAgImF1ZCI6ICJjb20uYWJjQ29tcGFueS5hcHBsZSIsCiAgImV4cCI6IDE1NzQ0NjA2NDgxNDYsCiAgImlhdCI6IDE1NzQzODA0NzUsCiAgInN1YiI6ICIwMDI0NTQuM2JmZmNiMzZjNmE5NDhhNTAxMDA4M2I4ZGE1ZjVmMzEuMTEyMyIsCiAgImNfaGFzaCI6ICJtVkZwWndhcFVhSG9kYkxUNXZaX2hnIiwKICAiZW1haWwiOiAiYWJjZEBlYmF5LmNvbSIsCiAgImVtYWlsX3ZlcmlmaWVkIjogInRydWUiLAogICJhdXRoX3RpbWUiOiAxNTc0MzgwNDc1CiAgIm5vbmNlIjogIjEyMzQiCn0.thisisafakesignature";
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.abcCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken, clientIds, "abcd");
            Assert.fail();
        } catch (IDTokenException e) {
            Assert.assertNotNull(e.getErrorEnum());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN, e.getErrorEnum());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN.getErrorId(), e.getErrorEnum().getErrorId());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN.getErrorMsg(), e.getErrorEnum().getErrorMsg());
        }
    }

    @Test
    public void testVerifyAppleIDTokenSignatureVerifiationFailure() {

        String idToken2 = TestUtils.createValidJWTWithFakeSignature(null, "com.abcCompany.webapp", "1234", System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.abcCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken2, clientIds, null);

        } catch (IDTokenException e) {
            Assert.assertNotNull(e.getErrorEnum());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN, e.getErrorEnum());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN.getErrorId(), e.getErrorEnum().getErrorId());
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN.getErrorMsg(), e.getErrorEnum().getErrorMsg());
        }

    }

    @Test
    public void testExtractAppleUserinfoFromIDToken() {

        long expTime = System.currentTimeMillis()+300000;

        // Build a JWT token with default values in TestUtils.
        String idToken = TestUtils.createValidJWTWithFakeSignature(null, null, null, expTime);
        AppleUserInfo appleUserInfo = null;
        try {
            appleUserInfo = appleIDTokenValidator.extractAppleUserinfoFromIDToken(idToken);
            Assert.assertNotNull(appleUserInfo);
            Assert.assertEquals(appleUserInfo.getIssuer(), TestUtils.getDefaultIssuer());
            Assert.assertEquals(appleUserInfo.getEmail(), TestUtils.getDefaultEmail());
            Assert.assertEquals(appleUserInfo.getNonce(), TestUtils.getDefaultNonce());
            Assert.assertNotNull(appleUserInfo.getExpiryTime());
            Assert.assertNotNull(appleUserInfo.getClientId());
            Assert.assertNotNull(appleUserInfo.getUniqueIdentifier());
            Assert.assertNotNull(appleUserInfo.getIssuingTime());
            Assert.assertTrue(Long.parseLong(appleUserInfo.getIssuingTime()) < System.currentTimeMillis());
            Assert.assertTrue(appleUserInfo.isEmailVerified());
            Assert.assertEquals(Long.parseLong(appleUserInfo.getExpiryTime()), expTime);

        } catch (IDTokenException e) {
            Assert.fail();
        }

    }

    @Test
    public void testExtractAppleUserinfoFromIDTokenNoSignature() {

        long expTime = System.currentTimeMillis()+300000;

        // Build a JWT token with default values in TestUtils.
        String idToken = TestUtils.createValidJWTWithNoSignature(null, null, null, expTime);
        AppleUserInfo appleUserInfo = null;
        try {
            appleUserInfo = appleIDTokenValidator.extractAppleUserinfoFromIDToken(idToken);
            Assert.fail();

        } catch (IDTokenException e) {
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN, e.getErrorEnum());
        }

    }

    @Test
    public void testExtractAppleUserinfoFromIDTokenNoPayload() {

        // Build a JWT token with default values in TestUtils.
        String idToken = "aksdjh15a6s1kjh..ert45p6md4l6";
        AppleUserInfo appleUserInfo = null;
        try {
            appleUserInfo = appleIDTokenValidator.extractAppleUserinfoFromIDToken(idToken);
            Assert.fail();

        } catch (IDTokenException e) {
            Assert.assertEquals(IDTokenErrorEnum.INVALID_ID_TOKEN, e.getErrorEnum());
        }

    }

    @Test
    public void testVerifyAppleIDTokenSignatureVerificationFailure() {

        String idToken2 = TestUtils.createValidJWTWithWrongSignature(null, "com.abcCompany.webapp", null, System.currentTimeMillis()+300000);
        boolean result = false;
        try {
            List<String> clientIds = new ArrayList<>();
            clientIds.add("com.abcCompany.webapp");
            result = appleIDTokenValidator.verifyAppleIDToken(idToken2, clientIds, null);

        } catch (IDTokenException e) {
            Assert.fail();
        }
        Assert.assertFalse(result);

    }

}
