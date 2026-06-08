package com.example.budget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.budget.ui.theme.BudgetTheme

class MainActivity : ComponentActivity() {
    var smsServiceIntent = Intent(this, TextListenerService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val m = Modifier.padding(innerPadding)
                    Column {
                        Greeting(name = "You", modifier = m)
                        Text(text = "hello", modifier = m)
                    }
                }
            }
        }
        startService(smsServiceIntent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BudgetTheme {
        Greeting("Dawg")
    }
}