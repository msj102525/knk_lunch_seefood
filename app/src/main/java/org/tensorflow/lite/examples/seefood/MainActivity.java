package org.tensorflow.lite.examples.seefood;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import org.tensorflow.lite.examples.seefood.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Handler handler = new Handler();
    private int retryCount = 0; // 시도 횟수
    private static final int MAX_RETRIES = 20; // 최대 시도 횟수 (10초 동안 시도, 500ms 간격)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // WebView 설정
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                checkElementExists(); // 요소 확인 시작
            }
        });

        binding.webView.setWebChromeClient(new WebChromeClient()); // 웹 페이지 로드 상태 추적
        binding.webView.loadUrl("https://pf.kakao.com/_fUPun/posts"); // 웹 페이지 로드
    }

    private void checkElementExists() {
        if (retryCount >= MAX_RETRIES) {
            Log.e("MainActivity", "Element not found after max retries");
            return; // 최대 시도 횟수 초과 시 종료
        }

        binding.webView.evaluateJavascript(
                "javascript:(function() { " +
                        "var elements = document.querySelectorAll('.wrap_fit_thumb'); " +
                        "if (elements.length > 0) { " +
                        "   var style = window.getComputedStyle(elements[0]); " +
                        "   return style.backgroundImage; " +
                        "} else { return null; } " +
                        "})()",
                value -> {
                    Log.d("MainActivity", "JavaScript value: " + value);
                    if (value != null && value.contains("url(")) {
                        String imageUrl = value.substring(value.indexOf("url(") + 4, value.indexOf(")", value.indexOf("url(")));
                        imageUrl = imageUrl.replace("\\\"", ""); // URL에서 불필요한 따옴표 제거
                        Log.d("MainActivity", "Extracted image URL: " + imageUrl);
                        Glide.with(MainActivity.this).load(imageUrl).into(binding.imageView);
                    } else {
                        retryCount++;
                        handler.postDelayed(this::checkElementExists, 500); // 500ms 후 다시 실행
                    }
                }
        );
    }
}
