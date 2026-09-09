package com.example.ars_nova

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.ars_nova.ui.theme.ArsnovaTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// =========================================================================
// 1. ESTADO DA AUTENTICAÇÃO (Auth State)
// =========================================================================
sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String, val email: String) : AuthState()
    data class SuccessMessage(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

// =========================================================================
// 2. VIEWMODEL (Lógica de Negócio e Conexão Firebase)
// =========================================================================
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(
                userId = currentUser.uid,
                email = currentUser.email ?: ""
            )
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos!")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.value = AuthState.Authenticated(
                        userId = user?.uid ?: "",
                        email = user?.email ?: ""
                    )
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Falha ao realizar login."
                    )
                }
            }
    }

    fun signUp(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Preencha e-mail e senha para cadastrar!")
            return
        }

        if (pass.length < 6) {
            _authState.value = AuthState.Error("A senha deve ter no mínimo 6 caracteres!")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    _authState.value = AuthState.Authenticated(
                        userId = user?.uid ?: "",
                        email = user?.email ?: ""
                    )
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Falha ao cadastrar conta no Firebase."
                    )
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
}

// =========================================================================
// 3. MAIN ACTIVITY
// =========================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()

        setContent {
            ArsnovaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. APP NAVIGATION (Gerenciamento de Telas)
// =========================================================================
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Authenticated -> {
            HomeScreen(
                modifier = modifier,
                userEmail = state.email,
                userId = state.userId,
                onSignOut = { authViewModel.signOut() }
            )
        }
        else -> {
            LoginScreen(
                modifier = modifier,
                authViewModel = authViewModel
            )
        }
    }
}

// =========================================================================
// 5. TELA DE LOGIN / CADASTRO (Com Identidade Visual Integrada)
// =========================================================================
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()

    LoginScreenContent(
        modifier = modifier,
        authState = authState,
        onLogin = { email, pass -> authViewModel.login(email, pass) },
        onSignUp = { email, pass -> authViewModel.signUp(email, pass) },
        onClearError = { authViewModel.clearError() }
    )
}

@Composable
fun LoginScreenContent(
    modifier: Modifier = Modifier,
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var senhaInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Cores da Identidade Visual do App (Verde Profundo / Dark Slate / Branco)
    val brandGreen = Color(0xFF1B4D3E)
    val accentGreen = Color(0xFF2E7D32)
    val lightBg = Color(0xFFF4F7F5)

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
            onClearError()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = lightBg
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Superior Decorativo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(brandGreen, accentGreen)
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo com fallback limpo
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "Logo Ars Nova",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ARS NOVA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Portal de Acesso",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card Principal do Form
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Autenticação",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = brandGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo de E-mail
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("E-mail") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "E-mail", tint = brandGreen)
                        },
                        singleLine = true,
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandGreen,
                            focusedLabelColor = brandGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Campo de Senha
                    OutlinedTextField(
                        value = senhaInput,
                        onValueChange = { senhaInput = it },
                        label = { Text("Senha") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Senha", tint = brandGreen)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = authState !is AuthState.Loading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandGreen,
                            focusedLabelColor = brandGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = brandGreen)
                    } else {
                        // Botão Entrar
                        Button(
                            onClick = { onLogin(emailInput, senhaInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandGreen)
                        ) {
                            Text("ENTRAR", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botão Cadastrar
                        OutlinedButton(
                            onClick = { onSignUp(emailInput, senhaInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = brandGreen)
                        ) {
                            Text("CADASTRAR E-MAIL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. TELA HOME (Demonstrativa Pós-Autenticação)
// =========================================================================
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userEmail: String,
    userId: String,
    onSignOut: () -> Unit
) {
    val brandGreen = Color(0xFF1B4D3E)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Usuário",
            tint = brandGreen,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Autenticado com Sucesso!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = brandGreen
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "E-mail Registrado:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = userEmail,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Firebase UID:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = userId,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("SAIR DA CONTA", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// =========================================================================
// 7. PREVIEW
// =========================================================================
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ArsnovaTheme {
        LoginScreenContent(
            authState = AuthState.Unauthenticated,
            onLogin = { _, _ -> },
            onSignUp = { _, _ -> },
            onClearError = {}
        )
    }
}