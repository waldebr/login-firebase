package com.example.ars_nova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ars_nova.ui.theme.ArsnovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArsnovaTheme {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    var usuario by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF4CAF50)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val image = painterResource(R.drawable.logo)
            Image(
                painter = image,
                contentDescription = "Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(150.dp)
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Greeting("LOGIN")

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                Arrangement.Center
            ) {
                TextField(
                    value = usuario,
                    onValueChange = { novoValor -> usuario = novoValor },
                    label = { Text("Usuário") },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                Arrangement.Center
            ) {
                TextField(
                    value = senha,
                    onValueChange = { novaSenha -> senha = novaSenha },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ActionButton(
                text = "ENTRAR",
                buttonColors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00008B),
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                // Ação ao clicar no botão
            }
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    modifier: Modifier = Modifier,
    buttonColors: ButtonColors = ButtonDefaults.buttonColors(),
    block: () -> Unit
) {
    ElevatedButton(
        onClick = block,
        shape = RoundedCornerShape(5.dp),
        colors = buttonColors,
        modifier = modifier
    ) {
        Text(text = text)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "TELA DE $name",
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ArsnovaTheme {
        App()
    }
}