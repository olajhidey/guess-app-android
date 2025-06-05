package com.taloscore.guessapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.taloscore.guessapp.ErrorBanner
import com.taloscore.guessapp.LoadingDialog
import com.taloscore.guessapp.OutlinedInputText
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.LoginForm
import com.taloscore.guessapp.data.model.RegisterForm
import com.taloscore.guessapp.utils.Constant
import com.taloscore.guessapp.utils.TokenManager
import com.taloscore.guessapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AuthViews"

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navHostController: NavHostController
) {
    var isLogin by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLogin) {
            LoginContent(
                toggleAuthContent = { isLogin = !isLogin },
                authViewModel = authViewModel,
                navHostController = navHostController
            )
        } else {
            RegisterContent(
                toggleAuthContent = { isLogin = !isLogin },
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    toggleAuthContent: () -> Unit,
    authViewModel: AuthViewModel,
    navHostController: NavHostController
) {

    val loginState by authViewModel.loginState.collectAsState()
    var usernameText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginState) {
        when (loginState) {
            is ApiState.Loading -> {
                showDialog = true
            }

            is ApiState.Success -> {
                showDialog = false
                val success = (loginState as ApiState.Success).data
                scope.launch {
                    TokenManager.writeToken(context, success.token)
                    TokenManager.writeToPreference(context, Constant.USERNAME_KEY, usernameText)
                    navHostController.currentBackStackEntry?.savedStateHandle?.set("token", success.token)
                    navHostController.navigate(AppScreen.DashboardScreen.route)
                }
            }

            is ApiState.Error -> {
                errorMessage = (loginState as ApiState.Error).message
                showDialog = false
                showError = true
                delay(3000)
                showError = false
            }

            else -> {}
        }
    }

    if (showDialog) {
        LoadingDialog()
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Guess the Image"
        )

        if (showError) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = modifier.padding(bottom = 8.dp))
        }

        OutlinedInputText(
            label = "Username",
            inputValue = usernameText,
            onValueChange = { usernameText = it },
            isPassword = false,
            modifier = modifier.padding(bottom = 8.dp)
        )
        OutlinedInputText(
            label = "Password",
            inputValue = passwordText,
            onValueChange = { passwordText = it },
            isPassword = true,
            modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
        )

        ButtonLayout(
            buttonText = "Login",
            modifier = modifier.padding(top = 4.dp),
            onClick = {
                authViewModel.performLogin(
                    LoginForm(
                        username = usernameText,
                        password = passwordText
                    )
                )
            },
            buttonType = ButtonType.LOGIN,
            enabled = if (usernameText.isEmpty() || passwordText.isEmpty()) false else true
        )
        ButtonLayout(
            buttonText = "Register",
            modifier = modifier.padding(top = 8.dp),
            onClick = toggleAuthContent,
            buttonType = ButtonType.REGISTER,
            enabled = true
        )
    }
}

@Composable
fun RegisterContent(
    modifier: Modifier = Modifier,
    toggleAuthContent: () -> Unit,
    authViewModel: AuthViewModel
) {
    val registerState by authViewModel.registerState.collectAsState()
    var usernameText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }
    var emailText by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        when (registerState) {
            is ApiState.Loading -> {
                showDialog = true
            }

            is ApiState.Success -> {
                showDialog = false
                val success = (registerState as ApiState.Success).data
                Log.e(TAG, "RegisterContent: ${success.message}")
                toggleAuthContent()
            }

            is ApiState.Error -> {
                errorMessage = (registerState as ApiState.Error).message
                showDialog = false
                showError = true
                delay(3000)
                showError = false
            }

            else -> {}
        }
    }

    if (showDialog) {
        LoadingDialog()
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (showError) {
            ErrorBanner(message = errorMessage)
            Spacer(modifier = modifier.padding(bottom = 8.dp))
        }

        OutlinedInputText(
            label = "Username",
            inputValue = usernameText,
            onValueChange = { usernameText = it },
            isPassword = false,
            modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        OutlinedInputText(
            label = "Email",
            inputValue = emailText,
            onValueChange = { emailText = it },
            isPassword = false,
            modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        OutlinedInputText(
            label = "Password",
            inputValue = passwordText,
            onValueChange = { passwordText = it },
            isPassword = true,
            modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
        )
        ButtonLayout(
            buttonText = "Register",
            modifier = modifier.padding(top = 8.dp, bottom = 8.dp),
            onClick = {
                val registerForm = RegisterForm(
                    username = usernameText,
                    password = passwordText,
                    email = emailText
                )
                authViewModel.performRegistration(registerForm)
            },
            buttonType = ButtonType.REGISTER,
            enabled = if (usernameText.isEmpty() || passwordText.isEmpty() || emailText.isEmpty()) false else true
        )
        ButtonLayout(
            buttonText = "Login",
            modifier = modifier.padding(top = 8.dp),
            onClick = toggleAuthContent,
            buttonType = ButtonType.LOGIN,
            enabled = true
        )
    }
}

@Composable
fun ButtonLayout(
    modifier: Modifier = Modifier,
    buttonText: String,
    onClick: () -> Unit,
    buttonType: ButtonType,
    enabled: Boolean
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        colors = buttonColors(
            containerColor = if (buttonType == ButtonType.REGISTER) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            contentColor = if (buttonType == ButtonType.REGISTER) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = buttonText, fontFamily = MaterialTheme.typography.bodyMedium.fontFamily)
    }
}

enum class ButtonType {
    REGISTER,
    LOGIN
}