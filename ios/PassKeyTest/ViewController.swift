import UIKit
import AuthenticationServices

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground

        let registerButton = UIButton(type: .system)
        registerButton.setTitle("Register Passkey", for: .normal)
        registerButton.addTarget(self, action: #selector(registerPasskey), for: .touchUpInside)

        let loginButton = UIButton(type: .system)
        loginButton.setTitle("Sign In with Passkey", for: .normal)
        loginButton.addTarget(self, action: #selector(signInPasskey), for: .touchUpInside)

        let stack = UIStackView(arrangedSubviews: [registerButton, loginButton])
        stack.axis = .vertical
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            stack.centerYAnchor.constraint(equalTo: view.centerYAnchor)
        ])
    }

    @objc private func registerPasskey() {
        let provider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: "example.com")
        let challenge = "register_challenge".data(using: .utf8)!
        let request = provider.createCredentialRegistrationRequest(challenge: challenge, name: nil, userID: UUID().uuidString.data(using: .utf8)!)
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.performRequests()
    }

    @objc private func signInPasskey() {
        let provider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: "example.com")
        let challenge = "login_challenge".data(using: .utf8)!
        let request = provider.createCredentialAssertionRequest(challenge: challenge)
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.performRequests()
    }
}
