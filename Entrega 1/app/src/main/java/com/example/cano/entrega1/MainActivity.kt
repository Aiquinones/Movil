package com.example.cano.entrega1

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, intent.getStringExtra("user"), Toast.LENGTH_SHORT).show()

        main_button.setOnClickListener{
            startActivity(LoginActivity.getIntent(this))
        }

    }



}
