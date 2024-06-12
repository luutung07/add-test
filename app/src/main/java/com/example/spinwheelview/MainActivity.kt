package com.example.spinwheelview

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.spinwheelview.databinding.ActivityMainBinding
import com.example.spinwheelview.widget.SPIN_TYPE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnChon.setOnClickListener {
            binding.sp.setType(SPIN_TYPE.CHOOSE)
        }
        binding.btnRank.setOnClickListener {
            binding.sp.setType(SPIN_TYPE.RANK)
        }
        binding.btnCap.setOnClickListener {
            binding.sp.setType(SPIN_TYPE.COUPLE)
        }

//        findViewById<CircleRippleButton>(R.id.circle_ripple_button).setOnClickListener {
//            findViewById<CircleRippleButton>(R.id.circle_ripple_button).setButtonSelected(
//                !findViewById<CircleRippleButton>(
//                    R.id.circle_ripple_button
//                ).isButtonSelected()
//            );
//        }
    }
}