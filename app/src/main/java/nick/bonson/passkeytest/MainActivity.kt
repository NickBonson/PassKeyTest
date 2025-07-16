package nick.bonson.passkeytest

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
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

interface PageService {
    @GET
    fun fetchPage(@Url url: String): Call<ResponseBody>
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .build()

        val service = retrofit.create(PageService::class.java)

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
    }
}
