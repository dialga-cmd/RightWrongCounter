package com.example.rightwrongcounter

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var topCount = 0
    private var midCount = 0
    private var bottomCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Fullscreen mode (Status bar + Navigation bar hidden)
        window.decorView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.hide(
                    android.view.WindowInsets.Type.statusBars() or
                            android.view.WindowInsets.Type.navigationBars()
                )
            } else {
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
        }

        // UI Components
        val topMinus: Button = findViewById(R.id.topMinus)
        val topPlus: Button = findViewById(R.id.topPlus)
        val midMinus: Button = findViewById(R.id.midMinus)
        val midPlus: Button = findViewById(R.id.midPlus)
        val bottomMinus: Button = findViewById(R.id.bottomMinus)
        val bottomPlus: Button = findViewById(R.id.bottomPlus)
        val resetButton: Button = findViewById(R.id.resetButton)

        val topCountText: TextView = findViewById(R.id.topCount)
        val midCountText: TextView = findViewById(R.id.midCount)
        val bottomCountText: TextView = findViewById(R.id.bottomCount)
        val totalText: TextView = findViewById(R.id.totalText)
        val pointsText: TextView = findViewById(R.id.pointsText)

        // Update UI function
        fun updateUI() {
            topCountText.text = topCount.toString()
            midCountText.text = midCount.toString()
            bottomCountText.text = bottomCount.toString()

            // Total sum of all three sections
            val total = topCount + midCount + bottomCount
            totalText.text = "Total: $total"

            // Points calculation
            val points = (topCount * 4) - (bottomCount)
            pointsText.text = "Points: $points"
        }

        // Initialize UI
        updateUI()

        // Button actions
        topMinus.setOnClickListener { topCount--; updateUI() }
        topPlus.setOnClickListener { topCount++; updateUI() }
        midMinus.setOnClickListener { midCount--; updateUI() }
        midPlus.setOnClickListener { midCount++; updateUI() }
        bottomMinus.setOnClickListener { bottomCount--; updateUI() }
        bottomPlus.setOnClickListener { bottomCount++; updateUI() }

        // Reset button
        resetButton.setOnClickListener {
            topCount = 0
            midCount = 0
            bottomCount = 0
            updateUI()
        }
    }
}
