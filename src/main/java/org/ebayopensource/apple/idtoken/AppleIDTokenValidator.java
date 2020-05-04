package org.ebayopensource.apple.idtoken;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ebayopensource.apple.idtoken.entities.AppleUserInfo;
import org.ebayopensource.apple.idtoken.util.IDTokenErrorEnum;
import org.ebayopensource.apple.idtoken.util.IDTokenException;

import java.security.PublicKey;
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
public class AppleIDTokenValidator {

    private static final String SEPARATOR_PERIOD = "\\.";
    private static final int ID_TOKEN_PARTS = 3;
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private static List<PublicKey> applePublicKeys = null;
    private ApplePublicKeysManager applePublicKeysManager;

    public AppleIDTokenValidator() {
        applePublicKeysManager = ApplePublicKeysManager.getApplePublicKeysManager();
    }

    /**
     * Function that verifies ID token's signature and content against client ids and nonce.
     *
     * @param idToken Input ID token
     * @param clientIds Client Ids to validate against
     * @param nonce Nonce to validate against
     * @return true/false based on validation
     * @throws IDTokenException
     */
    public boolean verifyAppleIDToken(String idToken, List<String> clientIds, String nonce) throws IDTokenException {

        if(isValidIDToken(idToken) && verifyTokenPayload(extractAppleUserinfoFromIDToken(idToken), clientIds, nonce)) {
            if (applePublicKeys == null) {
                applePublicKeys = applePublicKeysManager.getApplePublicKeys();
            }
            for (PublicKey publicKey : applePublicKeys) {
                if (verifySignature(idToken, publicKey)) {
                    return true;
                } else {
                    applePublicKeys = applePublicKeysManager.getApplePublicKeys();
                }
            }
        }
        return false;
    }

    private boolean verifySignature(String idToken, PublicKey publicKey) throws IDTokenException {
        try {
            Jwts.parser().setSigningKey(publicKey).parseClaimsJws(idToken);
        } catch (SignatureException e) {
            return false;
        } catch (MalformedJwtException e) {
            throw new IDTokenException(IDTokenErrorEnum.INVALID_ID_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new IDTokenException(IDTokenErrorEnum.INVALID_ID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new IDTokenException(IDTokenErrorEnum.EXPIRED_ID_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new IDTokenException(IDTokenErrorEnum.UNSUPPORTED_ID_TOKEN);
        } catch (Exception e) {
            throw new IDTokenException(IDTokenErrorEnum.INVALID_ID_TOKEN, e);
        }
        return true;
    }

    /**
     * Function that extracts user information from ID token.
     *
     * @param idToken Input ID token
     * @return AppleUserInfo object containing user details
     * @throws IDTokenException
     */
    public AppleUserInfo extractAppleUserinfoFromIDToken(String idToken) throws IDTokenException {
        AppleUserInfo appleUserInfo = null;
        if(isValidIDToken(idToken)) {
            try {
                String payload = new String(Base64.decodeBase64(idToken.split(SEPARATOR_PERIOD)[1]));
                appleUserInfo = new Gson().fromJson(payload, AppleUserInfo.class);
            } catch (JsonSyntaxException e) {
                throw new IDTokenException(IDTokenErrorEnum.INVALID_ID_TOKEN);
            }
            if (appleUserInfo == null) throw new IDTokenException(IDTokenErrorEnum.INVALID_ID_TOKEN);
            return appleUserInfo;
        } else {
            throw new IDTokenException(IDTokenErrorEnum.INVALID_ID_TOKEN);
        }
    }

    private boolean isValidIDToken(String idToken) {
    	if (StringUtils.isBlank(idToken) || idToken.length() < 5) return false;
    	String[] idTokenStrings = idToken.split(SEPARATOR_PERIOD);
        if (idTokenStrings == null 
        		|| idTokenStrings.length != ID_TOKEN_PARTS
                || StringUtils.isBlank(idTokenStrings[0])
                || StringUtils.isBlank(idTokenStrings[1])
                || StringUtils.isBlank(idTokenStrings[2])){
            return false;
        }
        return true;
    }

    /**
        Verify the JWS E256 signature using the server’s public key

        Verify the nonce for the authentication

        Verify that the iss field contains https://appleid.apple.com

        Verify that the aud field is the developer’s client_id

        Verify that the time is earlier than the exp value of the token
     */
    private boolean verifyTokenPayload(AppleUserInfo appleUserInfo, List<String> originalClientIds, String originalNonce) {

        boolean isValidIssuer = !StringUtils.isBlank(appleUserInfo.getIssuer())
                                && appleUserInfo.getIssuer().contains(APPLE_ISSUER);

        boolean isValidClientId = true;
        if(originalClientIds != null) {
            isValidClientId = !StringUtils.isBlank(appleUserInfo.getClientId())
                    && originalClientIds.contains(appleUserInfo.getClientId());
        }

        boolean isValidNonce = StringUtils.isBlank(originalNonce) ? true : (!StringUtils.isBlank(appleUserInfo.getNonce())
                                && appleUserInfo.getNonce().equals(originalNonce));

        boolean isValidExpirationTime = StringUtils.isNumeric(appleUserInfo.getExpiryTime());

        return isValidIssuer && isValidClientId && isValidNonce && isValidExpirationTime;
    }

}
