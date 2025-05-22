package com.example.poppywrapper

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.Button
import android.widget.Toast
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var textButton: Button
    private lateinit var recordButton: Button
    private lateinit var webView: WebView

    private val REQUEST_CODE_PERMISSIONS = 101

    // Dynamic list: only include permissions valid for API level
    private val REQUIRED_PERMISSIONS: Array<String>
        get() {
            val list = mutableListOf(
                Manifest.permission.RECORD_AUDIO
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                list.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                list.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            return list.toTypedArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ask all needed permissions at startup
        if (!allPermissionsGranted()) {
            requestMissingPermissions()
        }

        webView = findViewById(R.id.webview)

        // Enable cookies
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        // WebView setup
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                // Grant all requested permissions (mic, cam, etc)
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

        // Bottom bar buttons
        textButton = findViewById(R.id.button_text)
        recordButton = findViewById(R.id.button_record)

        textButton.setOnClickListener {
            sendKeyDownUpToWebView("T")
        }
        recordButton.setOnClickListener {
            sendKeyDownUpToWebView("R")
        }

        // Optional: Prompt user if notifications are disabled in settings
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.areNotificationsEnabled()) {
                Toast.makeText(
                    this,
                    "Notifications are disabled for this app. Please enable them for full functionality.",
                    Toast.LENGTH_LONG
                ).show()
                // Uncomment if you want to open notification settings automatically:
                /*
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                startActivity(intent)
                */
            }
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

                // Try clicking a button with the word RECORD if present
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
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestMissingPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Some permissions were not granted. The app may not work correctly.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
