# PassKeyTest

This project demonstrates a simple implementation of passkeys on Android and iOS.

## Android
- Uses Retrofit and OkHttp for networking.
- Passkey support via `CredentialManager` from the `androidx.credentials` library.
- Layout uses ViewBinding.

## iOS
- UIKit based interface.
- Passkey support implemented with `ASAuthorizationPlatformPublicKeyCredentialProvider`.
- Networking done with `URLSession` where needed.

Binary resources (images etc.) are untouched; add them manually if needed.
