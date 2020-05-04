package org.ebayopensource.apple.idtoken;

import java.security.PublicKey;
import java.util.List;

import org.ebayopensource.apple.idtoken.util.IDTokenErrorEnum;
import org.ebayopensource.apple.idtoken.util.IDTokenException;
import org.junit.Assert;
import org.junit.Test;


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

public class ApplePublicKeysManagerTest {

    private ApplePublicKeysManager applePublicKeysManager = ApplePublicKeysManager.getApplePublicKeysManager();

    @Test
    public void testgetApplePublicKeysSuccess() {
        try {
            applePublicKeysManager.setRefreshSkewMilliseconds(10000);
            applePublicKeysManager.setProxyEnabled(false);
            Assert.assertTrue(!applePublicKeysManager.isProxyEnabled());
            Assert.assertTrue("incorrect port", (applePublicKeysManager.getRefreshSkewMilliseconds() == 10000));

            List<PublicKey> keyList = applePublicKeysManager.getApplePublicKeys();

            Assert.assertTrue("keylist is null", keyList != null);
            Assert.assertTrue("keylist size is incorrect", keyList.size() > 0);

            List<PublicKey> keyList2 = applePublicKeysManager.getApplePublicKeys();

            Assert.assertTrue("keylist is null", keyList2 != null);
            Assert.assertTrue("keylist size is incorrect", keyList2.size() > 0);

            applePublicKeysManager.setRefreshSkewMilliseconds(0);

            List<PublicKey> keyList3 = applePublicKeysManager.getApplePublicKeys();

            Assert.assertTrue("keylist is null", keyList3 != null);
            Assert.assertTrue("keylist size is incorrect", keyList3.size() > 0);

        } catch (Exception e) {
            Assert.fail();
        } finally {
        	resetProxConfig();
        }

    }

    @Test
    public void testProxySetupSuccess() {
        try {
            applePublicKeysManager.setProxyHost("testhost").setProxyPort(8080).setRefreshSkewMilliseconds(10000);
            
            Assert.assertTrue(applePublicKeysManager.isProxyEnabled());
            Assert.assertTrue("incorrect host", "testhost".equalsIgnoreCase(applePublicKeysManager.getProxyHost()));
            Assert.assertTrue("incorrect port", (applePublicKeysManager.getProxyPort() == 8080));
            Assert.assertTrue("incorrect port", (applePublicKeysManager.getRefreshSkewMilliseconds() == 10000));

        } catch (Exception e) {
            Assert.fail();
        } finally {
        	resetProxConfig();
        }
        
    }

    @Test
    public void testProxySetupInvalidPort() {
        try {
            applePublicKeysManager.setProxyHost("testhost").setProxyPort(-8080);
            Assert.fail();
            
        } catch (IDTokenException e) {
            Assert.assertTrue("incorrect error", e!= null && e.getErrorEnum().equals(IDTokenErrorEnum.PROXY_SETUP_ERROR));
        } catch (Exception e) {
        	Assert.fail();
        } finally {
        	resetProxConfig();
        }
    }
    
    @Test
    public void testProxySetupNullProxyHost() {
        try {
            applePublicKeysManager.setProxyHost(null);
            Assert.fail();
            
        } catch (IDTokenException e) {
            Assert.assertTrue("incorrect error", e!= null && e.getErrorEnum().equals(IDTokenErrorEnum.PROXY_SETUP_ERROR));
        } catch (Exception e) {
        	Assert.fail();
        } finally {
        	resetProxConfig();
        }
    }
    
    @Test
    public void testProxyAPIUnavailable() {
        try {
            applePublicKeysManager.setProxyHost("testhost").setProxyPort(8080).setRefreshSkewMilliseconds(10000);
            applePublicKeysManager.refreshApplePublicKeys();
            
            Assert.fail();

        } catch (IDTokenException e) {
            Assert.assertTrue("incorrect error", e!= null && e.getErrorEnum().equals(IDTokenErrorEnum.APPLE_PUBLIC_KEY_UNAVAILABLE));
        } catch (Exception e) {
        	Assert.fail();
        } finally {
        	resetProxConfig();
        }

    }

    private void resetProxConfig() {
    	applePublicKeysManager.setProxyEnabled(false);

    }

}
