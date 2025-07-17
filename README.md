# PassKeyTest

This project demonstrates a simple implementation of passkeys on Android and iOS. It now also includes a basic example of using the FIDO2 API on Android.

## Android
- Uses Retrofit and OkHttp for networking.
- Passkey support via `CredentialManager` from the `androidx.credentials` library.
- Requires the Play Services auth provider dependency `androidx.credentials:play-services-auth`.
- FIDO2 integration via `com.google.android.gms:play-services-fido` with simple register and sign in helpers.
- Layout uses ViewBinding.

## iOS
- UIKit based interface.
- Passkey support implemented with `ASAuthorizationPlatformPublicKeyCredentialProvider`.
- Networking done with `URLSession` where needed.

Binary resources (images etc.) are untouched; add them manually if needed.
