package com.example.poppywrapper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var textButton: Button
    private lateinit var recordButton: Button
    private lateinit var buttonChevron: ImageButton
    private lateinit var buttonChevronRestore: ImageButton
    private lateinit var bottomBar: LinearLayout

    private lateinit var socialButton: Button
    private lateinit var aiChatButton: Button
    private lateinit var buttonChevronTop: ImageButton
    private lateinit var buttonChevronRestoreTop: ImageButton
    private lateinit var topBar: LinearLayout

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }

        webView = findViewById(R.id.webview)

        // Bottom bar
        bottomBar = findViewById(R.id.bottom_bar)
        textButton = findViewById(R.id.button_text)
        recordButton = findViewById(R.id.button_record)
        buttonChevron = findViewById(R.id.button_chevron)
        buttonChevronRestore = findViewById(R.id.button_chevron_restore)

        // Top bar
        topBar = findViewById(R.id.top_bar)
        socialButton = findViewById(R.id.button_social)
        aiChatButton = findViewById(R.id.button_ai_chat)
        buttonChevronTop = findViewById(R.id.button_chevron_top)
        buttonChevronRestoreTop = findViewById(R.id.button_chevron_restore_top)

        // WebView setup
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(true)
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36"
        webView.loadUrl("https://app.getpoppy.ai")

        // Bottom bar handlers
        textButton.setOnClickListener {
            sendKeyDownUpToWebView("T")
        }
        recordButton.setOnClickListener {
            sendKeyDownUpToWebView("R")
        }
        buttonChevron.setOnClickListener {
            bottomBar.visibility = View.GONE
            buttonChevronRestore.visibility = View.VISIBLE
        }
        buttonChevronRestore.setOnClickListener {
            bottomBar.visibility = View.VISIBLE
            buttonChevronRestore.visibility = View.GONE
        }

        // Top bar handlers
        socialButton.setOnClickListener {
            sendKeyDownUpToWebView("S")
        }
        aiChatButton.setOnClickListener {
            sendKeyDownUpToWebView("C")
        }
        buttonChevronTop.setOnClickListener {
            topBar.visibility = View.GONE
            buttonChevronRestoreTop.visibility = View.VISIBLE
        }
        buttonChevronRestoreTop.setOnClickListener {
            topBar.visibility = View.VISIBLE
            buttonChevronRestoreTop.visibility = View.GONE
        }
    }

    private fun sendKeyDownUpToWebView(keyChar: String) {
        val js = """
            (function() {
                document.body.focus();
                var keyDown = new KeyboardEvent('keydown', {
                    key: '$keyChar',
                    code: 'Key$keyChar',
                    keyCode: '$keyChar'.charCodeAt(0),
                    which: '$keyChar'.charCodeAt(0),
                    bubbles: true,
                    cancelable: true
                });
                var keyUp = new KeyboardEvent('keyup', {
                    key: '$keyChar',
                    code: 'Key$keyChar',
                    keyCode: '$keyChar'.charCodeAt(0),
                    which: '$keyChar'.charCodeAt(0),
                    bubbles: true,
                    cancelable: true
                });
                document.dispatchEvent(keyDown);
                document.dispatchEvent(keyUp);

                // Try clicking a button with RECORD if present
                var recBtn = Array.from(document.querySelectorAll('button,div'))
                    .find(el => el.innerText && el.innerText.toUpperCase().includes('RECORD'));
                if('$keyChar' === 'R' && recBtn) { recBtn.click(); }
            })();
        """
        webView.evaluateJavascript(js, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_T -> {
                    textButton.performClick()
                    return true
                }
                KeyEvent.KEYCODE_R -> {
                    recordButton.performClick()
                    return true
                }
                KeyEvent.KEYCODE_S -> {
                    socialButton.performClick()
                    return true
                }
                KeyEvent.KEYCODE_C -> {
                    aiChatButton.performClick()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
