package nick.bonson.passkeytest

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.CreatePublicKeyCredentialRequest
import kotlinx.coroutines.launch
import nick.bonson.passkeytest.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import okhttp3.ResponseBody
import android.util.Base64
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.Fido2ApiClient
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions

interface PageService {
    @GET
    fun fetchPage(@Url url: String): Call<ResponseBody>
}

interface PasskeyService {
    @GET("register")
    fun register(): Call<ResponseBody>

    @GET("login")
    fun login(): Call<ResponseBody>
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var passkeyService: PasskeyService
    private lateinit var fido2Client: Fido2ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        credentialManager = CredentialManager.create(this)
        fido2Client = Fido.getFido2ApiClient(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .build()

        val service = retrofit.create(PageService::class.java)
        passkeyService = retrofit.create(PasskeyService::class.java)

        service.fetchPage("https://example.com").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val html = response.body()?.string()
                if (html != null) {
                    runOnUiThread {
                        binding.webView.webViewClient = WebViewClient()
                        binding.webView.loadDataWithBaseURL("https://example.com", html, "text/html", "utf-8", null)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                runOnUiThread {
                    binding.webView.webViewClient = WebViewClient()
                    binding.webView.loadUrl("https://example.com")
                }
            }
        })

        binding.registerButton.setOnClickListener { registerPasskey() }
        binding.loginButton.setOnClickListener { loginPasskey() }
        binding.fidoRegisterButton.setOnClickListener { registerFido2() }
        binding.fidoLoginButton.setOnClickListener { loginFido2() }
    }

    private fun registerPasskey() {
        lifecycleScope.launch {
            val userId = "demo-user"
            val userIdBase64 = Base64.encodeToString(userId.toByteArray(), Base64.NO_WRAP)
            val requestJson = """
                {
                  "challenge": "register_challenge",
                  "rp": {
                    "name": "Example",
                    "id": "example.com"
                  },
                  "user": {
                    "id": "$userIdBase64",
                    "name": "user@example.com",
                    "displayName": "Example User"
                  },
                  "pubKeyCredParams": [{"type": "public-key", "alg": -7}],
                  "attestation": "none"
                }
            """.trimIndent()

            val request = CreatePublicKeyCredentialRequest(requestJson)
            credentialManager.createCredential(this@MainActivity, request)
            passkeyService.register()
        }
    }

    private fun loginPasskey() {
        lifecycleScope.launch {
            val optionJson = """
                {
                  "challenge": "login_challenge",
                  "rpId": "example.com"
                }
            """.trimIndent()

            val option = GetPublicKeyCredentialOption(optionJson)
            val request = GetCredentialRequest(listOf(option))
            credentialManager.getCredential(this@MainActivity, request)
            passkeyService.login()
        }
    }

    private fun registerFido2() {
        val challenge = "fido2_register".toByteArray()
        val options = PublicKeyCredentialCreationOptions.Builder()
            .setRp(PublicKeyCredentialRpEntity("example.com", "Example", null))
            .setUser(
                PublicKeyCredentialUserEntity(
                    challenge,
                    "user@example.com",
                    null,
                    "Example User"
                )
            )
            .setChallenge(challenge)
            .build()

        fido2Client.getRegisterPendingIntent(options)
            .addOnSuccessListener { pendingIntent ->
                startIntentSenderForResult(pendingIntent.intentSender, 2001, null, 0, 0, 0)
            }
    }

    private fun loginFido2() {
        val challenge = "fido2_login".toByteArray()
        val options = PublicKeyCredentialRequestOptions.Builder()
            .setRpId("example.com")
            .setChallenge(challenge)
            .build()

        fido2Client.getSignPendingIntent(options)
            .addOnSuccessListener { pendingIntent ->
                startIntentSenderForResult(pendingIntent.intentSender, 2002, null, 0, 0, 0)
            }
    }
}
