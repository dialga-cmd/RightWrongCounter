package com.example.rightwrongcounter

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var topCount = 0
    private var midCount = 0
    private var bottomCount = 0
    private var isJeeMode = true
    private var startTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        setContentView(R.layout.activity_main)

        // Request storage permission
        requestAllFilesAccessPopup()

        // UI elements
        val topMinus: Button = findViewById(R.id.topMinus)
        val topPlus: Button = findViewById(R.id.topPlus)
        val midMinus: Button = findViewById(R.id.midMinus)
        val midPlus: Button = findViewById(R.id.midPlus)
        val bottomMinus: Button = findViewById(R.id.bottomMinus)
        val bottomPlus: Button = findViewById(R.id.bottomPlus)
        val totalBox: TextView = findViewById(R.id.totalBox)
        val pointsBox: TextView = findViewById(R.id.pointsBox)
        val saveButton: FloatingActionButton = findViewById(R.id.saveButton)
        val menuButton: FloatingActionButton = findViewById(R.id.menuButton)

        val topCountText: TextView = findViewById(R.id.topCount)
        val midCountText: TextView = findViewById(R.id.midCount)
        val bottomCountText: TextView = findViewById(R.id.bottomCount)

        // Update UI function
        val updateUI = {
            topCountText.text = "$topCount"
            midCountText.text = "$midCount"
            bottomCountText.text = "$bottomCount"

            val total = topCount + midCount + bottomCount
            totalBox.text = "Total: $total"

            if (isJeeMode) {
                val points = (topCount * 4) + (bottomCount * (-1))
                pointsBox.text = "Points: ($points/100)"

                if (points >= 100 || total >= 25) {
                    showResultPopup(points)
                }
            } else {
                pointsBox.text = "Normal Mode"
            }
        }

        // Button listeners
        topMinus.setOnClickListener { topCount--; updateUI() }
        topPlus.setOnClickListener { topCount++; updateUI() }
        midMinus.setOnClickListener { midCount--; updateUI() }
        midPlus.setOnClickListener { midCount++; updateUI() }
        bottomMinus.setOnClickListener { bottomCount--; updateUI() }
        bottomPlus.setOnClickListener { bottomCount++; updateUI() }

        // Save button
        saveButton.setOnClickListener {
            if (isJeeMode) {
                val points = (topCount * 4) + (bottomCount * (-1))
                showResultPopup(points)
            } else {
                Toast.makeText(this, "No save in Normal mode", Toast.LENGTH_SHORT).show()
            }
        }

        // Menu button
        menuButton.setOnClickListener {
            showModeSelector()
        }

        updateUI()
    }

    private fun requestAllFilesAccessPopup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs storage permission to save your progress as PDFs. Allow now?")
                    .setPositiveButton("Allow") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    private fun showModeSelector() {
        val dialogView = layoutInflater.inflate(R.layout.mode_selector, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.jeeMode).setOnClickListener {
            isJeeMode = true
            Toast.makeText(this, "JEE Mode Activated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.normalMode).setOnClickListener {
            isJeeMode = false
            Toast.makeText(this, "Normal Mode Activated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showResultPopup(points: Int) {
        val dialogView = layoutInflater.inflate(R.layout.popup_fancy, null)
        val message = dialogView.findViewById<TextView>(R.id.popupMessage)
        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)

        // Format time
        val elapsedTime = System.currentTimeMillis() - startTime
        val hours = elapsedTime / 3600000
        val minutes = (elapsedTime % 3600000) / 60000
        val seconds = (elapsedTime % 60000) / 1000
        val timeFormatted = String.format("%02dhr:%02dmin:%02dsec", hours, minutes, seconds)

        message.text = "🎯 You scored $points/100 in $timeFormatted"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text.toString().ifEmpty {
                    "Result_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}"
                }
                savePdf(name, points, timeFormatted)
            }
            .setNegativeButton("Close") { d, _ -> d.dismiss() }
            .create()

        dialog.show()
    }

    private fun savePdf(fileName: String, points: Int, timeFormatted: String) {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()
        paint.textSize = 18f
        canvas.drawText("Right Wrong Counter - JEE Mode", 50f, 50f, paint)
        canvas.drawText("Name: $fileName", 50f, 90f, paint)
        canvas.drawText("Points: $points/100", 50f, 130f, paint)
        canvas.drawText("Time Taken: $timeFormatted", 50f, 170f, paint)
        canvas.drawText("Top Count: $topCount", 50f, 210f, paint)
        canvas.drawText("Middle Count: $midCount", 50f, 250f, paint)
        canvas.drawText("Bottom Count: $bottomCount", 50f, 290f, paint)

        doc.finishPage(page)

        // Create folder in root directory
        val dir = File(Environment.getExternalStorageDirectory().absolutePath + "/RightWrongChecker")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "$fileName.pdf")
        doc.writeTo(FileOutputStream(file))
        doc.close()

        Toast.makeText(this, "PDF saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
}
