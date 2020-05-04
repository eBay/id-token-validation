# id-token-validation
The ID Token validation library allows social sign-in to other third party services. Currently it is configured to allow sign-in to Apple's APIs and services, though other third party services are contemplated in the future.

### Description
The ID Token validation library is a simple and easy-to-use library to verify a signature and validate a payload of ID Tokens (JWT token) used during the 'Sign-in with Apple' feature.

### Links/Wikis
1. [Sign in with Apple REST API](https://developer.apple.com/documentation/signinwithapplerestapi)
2. [Authenticating Users with Sign in with Apple](https://developer.apple.com/documentation/signinwithapplerestapi/authenticating_users_with_sign_in_with_apple)
3. [Info on JWT](https://jwt.io/)
4. [Info on ID Tokens](https://www.oauth.com/oauth2-servers/openid-connect/id-tokens/)
5. [Verify Apple ID Token](https://developer.apple.com/documentation/signinwithapplerestapi/verifying_a_user)


### Supported Languages
This is created as a Maven based Java project and can be used as a dependency in a Java based application or other JVM based languages such as Groovy, Scala etc.,

### Installation
Current Version : 1.0.0-SNAPSHOT

Add following to <dependencies/> section of your pom.xml -
```
<dependency>
    <groupId>org.ebayopensource.social.auth</groupId>
    <artifactId>apple-id-token-validation</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Library Setup
The ID Token validation library validates the token by doing 2 steps:
1. Verify signature of token for Apple services
2. Verify payload of token for Apple services

For signature verification, the library fetches the Apple public key from the Apple website. More information on the same can be found in [official Apple documentation](https://developer.apple.com/documentation/signinwithapplerestapi/fetch_apple_s_public_key_for_verifying_token_signature).
Consumers of this library need to look at their connectivity needs from their own servers to an Apple endpoint for fetching the public key.

This library provides a way for consumers to set up connectivity parameters such as:
* `proxyHost`
* `proxyPort`

Refreshing (by re-fetching) Apple's public key happens periodically. This library provides a way for consumers to force refresh fetching of Apple's public key. 
* `refreshSkewMilliseconds` - Time in milliseconds after which Apple's public key is fetched again. If this value is not set, then it defaults to 86400000.

Here is a sample way to initialize above parameters during server initialization:
```
ApplePublicKeysManager applePublicKeysManager = ApplePublicKeysManager.getApplePublicKeysManager()
                                                    .setProxyHost("https://sample.proxy.host.com")
                                                    .setProxyPort(1000)
                                                    .setRefreshSkewMilliseconds(86400000);
```

### Usage
This library exposes 2 methods:
1. `public boolean verifyAppleIDToken(String idToken, List<String> clientIds, String nonce) throws IDTokenException`
2. `public AppleUserInfo extractAppleUserinfoFromIDToken(String idToken) throws IDTokenException`

#### `verifyAppleIDToken`
`verifyAppleIDToken` method verifies the signature of the ID Token (JWT token) by fetching Apple's public key and then validating the signature part of JWT token. If the input JWT token fails signature verification, then the function will return false. If it succeeds then it returns true.

This function returns `false` in following cases:
* Token is empty, `null`, token length is less than 5 or the count of separator period `.` is not 2.
* Issuer `iss` in token is empty or does not match with Apple issuer `https://appleid.apple.com`
* Client Id `aud` (which is used to register on Apple developer's account) in token does not match with the list of `clientIds` provided as an input to the method signature. NOTE: This can be skipped by passing the `clientIds` as `null`.
* Nonce `nonce` in token is empty or does not match with the nonce provided as an input to the method signature. NOTE: This can be skipped by passing the `nonce` as `null`.
* Expiration time `exp` in token is empty or not numeric.
* Signature verification fails.

For cases, when the token is expired or malformed, or is an exception is encountered during the process, then `IDTokenException` is thrown.


#### `extractAppleUserinfoFromIDToken`
`extractAppleUserinfoFromIDToken` method extracts the user information from the ID Token and returns it back with an object of type `AppleUserInfo`. This function simply decodes the payload and returns the values in the paylod without performing any signature validation.

Any malformed token will result in `IDTokenException`.  

### Error and exception details
Refer the [IDTokenErrorEnum](https://github.corp.ebay.com/AXIS/apple-id-token-validation/blob/master/src/main/java/org/ebayopensource/apple/idtoken/util/IDTokenErrorEnum.java) class for more details.

| Error Id  | ErrorEnum  | Description  | When to expect  |   |
|---|---|---|---|---|
| 50100 | INVALID_ID_TOKEN_SIGNATURE  | ID Token signature verification failed  | Signature verification of input ID token fails |   |
| 50200 | INVALID_ID_TOKEN | Invalid ID Token  | ID Token is malformed, payload verification fails, basic token validation failure |   |
| 50300 | EXPIRED_ID_TOKEN | Expired ID Token | ID token is expired. Current time is past `exp` value is payload.  |   |
| 50400 | UNSUPPORTED_ID_TOKEN | Unsupported exception in Apple ID Token validation | Unsupported exception in Apple ID Token validation |   |
| 60100 | PROXY_SETUP_ERROR | Invalid proxy host and/or port | Proxy host is null/empty or port is incorrect |   |
| 60200 | APPLE_PUBLIC_KEY_NOT_INITIALIZED | Apple public key not initialized |   |   |
| 60300 | APPLE_PUBLIC_KEY_UNAVAILABLE  | Invalid response from Apple during public key initialization  |   |   |
| 60400 | APPLE_SIGNIN_PUBLIC_KEY_ERROR  | Error during initializing public key  |   |   |

### Future enhancements
1. Connect timeout flexibility
2. Beautify 
3. Function/class/variables descriptions
4. Final review (especially error handling)
5. Final documentation (minor updates)
6. Client Id changes review

### Contribution
Contributions in terms of patches, features, or comments are always welcome. Refer to [contributing guidelines]() for more information. Submit Github issues for any feature enhancements, bugs, or documentation problems as well as questions and comments.

### License
Copyright (c) 2019-2020 eBay Inc.

Use of this source code is governed by a Apache-2.0 license that can be found in the LICENSE file or at https://opensource.org/licenses/Apache-2.0.
