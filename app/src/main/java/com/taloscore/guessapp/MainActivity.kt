package com.taloscore.guessapp

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.CategoryResponse
import com.taloscore.guessapp.data.model.LoginForm
import com.taloscore.guessapp.data.model.RegisterForm
import com.taloscore.guessapp.data.model.TopicResponse
import com.taloscore.guessapp.ui.screen.AppNavigation
import com.taloscore.guessapp.ui.screen.AppScreen
import com.taloscore.guessapp.ui.screen.DashboardScreen
import com.taloscore.guessapp.ui.screen.GameSummaryContent
import com.taloscore.guessapp.ui.screen.LoginContent
import com.taloscore.guessapp.ui.screen.RegisterContent
import com.taloscore.guessapp.ui.theme.GuessAppTheme
import com.taloscore.guessapp.utils.Constant
import com.taloscore.guessapp.utils.TokenManager
import com.taloscore.guessapp.viewmodel.AuthViewModel
import com.taloscore.guessapp.viewmodel.FinishedData
import com.taloscore.guessapp.viewmodel.GameViewModel
import com.taloscore.guessapp.viewmodel.Question
import com.taloscore.guessapp.viewmodel.Result
import com.taloscore.guessapp.viewmodel.WebsocketViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

private const val TAG = "MainActivity"


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }
        setContent {
            val token by viewModel.token.collectAsState()
            GuessAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        token = token,
                        authViewModel = viewModel
                    )
                }
            }
        }
    }
}

@Parcelize
data class GameInfo(
    val gameCode: String,
    val isHost: Boolean,
    val topicId: String,
    val categoryId: String,
    val username: String
) : Parcelable


@Composable
fun OptionButton(
    option: String,
    selectedOption: String?,
    isCorrect: Boolean?,
    onOptionSelected: (String) -> Unit
) {
    val backgroundColor = when {
        selectedOption == option && isCorrect == true -> Color(0xFF198754) // Green
        selectedOption == option && isCorrect == false -> Color(0xFFDC3545) // Red
        else -> Color.Transparent
    }

    OutlinedButton(
        onClick = { onOptionSelected(option) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = selectedOption == "",
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(text = option, style = MaterialTheme.typography.bodyLarge)
    }
}



fun generateCode(): String {
    return (1..6)
        .map { ('A'..'Z') + ('0'..'9') }
        .flatten()
        .shuffled()
        .take(6)
        .joinToString("")
}

@Composable
fun CreateGameScreen(
    categories: List<CategoryResponse>,
    topics: List<TopicResponse>,
    newGame: NewGameState,
    onCategoryChange: (String) -> Unit,
    onTopicChange: (String) -> Unit,
    onGenerateGameCode: () -> Unit,
    onStartGame: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(newGame.category) }
    var selectedTopic by remember { mutableStateOf(newGame.topic) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val copiedText = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Create a New Game",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Category Dropdown
        Text("Select Category", style = MaterialTheme.typography.labelMedium)
        DropdownMenuField(
            label = "-- Choose Category --",
            items = categories.map { it.name to it.ID },
            selectedId = selectedCategory,
            onItemSelected = {
                selectedCategory = it
                onCategoryChange(it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Topic Dropdown
        Text("Enter Topic", style = MaterialTheme.typography.labelMedium)
        DropdownMenuField(
            label = "-- Choose Topic --",
            items = topics.map { it.name to it.ID },
            selectedId = selectedTopic,
            onItemSelected = {
                selectedTopic = it
                onTopicChange(it)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onGenerateGameCode() },
            modifier = Modifier.fillMaxWidth(),
            enabled = if (selectedCategory == "" || selectedTopic == "") false else true
        ) {
            Text("Generate Game Code")
        }

        // Show game code and Start Game
        newGame.code?.let { code ->
            if (code != "") {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD4EDDA), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Game Code: ", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = code,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            copiedText.value = true
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT)
                                .show()
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_content_copy_24),
                                contentDescription = "Copy",
                                tint = Color.Gray
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onStartGame() },
                        colors = buttonColors(containerColor = Color(0xFF28A745))
                    ) {
                        Text("Start Game")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuField(
    label: String,
    items: List<Pair<String, String>>,
    selectedId: String,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedLabel by remember {
        mutableStateOf(items.find { it.second == selectedId }?.first ?: label)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { (name, id) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        selectedLabel = name
                        expanded = false
                        onItemSelected(id)
                    }
                )
            }
        }
    }
}

data class Category(val ID: String, val name: String)
data class Topic(val ID: String, val name: String)
data class NewGameState(
    var category: String = "",
    var topic: String = "",
    var code: String? = null
)


@Composable
fun OutlinedInputText(
    label: String,
    inputValue: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    isPassword: Boolean
) {
    OutlinedTextField(
        value = inputValue,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondary,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondary,
            disabledContainerColor = Color.White,
            errorContainerColor = Color.Red
        ),
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        placeholder = { Text(text = label) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun LoadingDialog(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = modifier
                    .width(100.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = modifier.padding(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading...",
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
        }
    }

}

@Composable
fun ErrorBanner(modifier: Modifier = Modifier, message: String = "Error") {
    Column(
        modifier = modifier
            .background(color = Color.Red)
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = MaterialTheme.typography.labelMedium.fontSize
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun GreetingPreview() {
    GuessAppTheme {
//        LoginContent(toggleAuthContent = {})
//        RegisterContent(toggleAuthContent = {})
//        LoadingDialog()
//        ErrorBanner()
        val navController = rememberNavController()
        val listParticipant = mutableListOf<Result>()
        listParticipant.add(
            Result(
                player_name = "Olajide",
                player_score = "10",
                code = "HTESXESS",
                topic_id = "1",
            )
        )
        listParticipant.add(
            Result(
                player_name = "Olajide",
                player_score = "10",
                code = "HTESXESS",
                topic_id = "1",
            )
        )
        listParticipant.add(
            Result(
                player_name = "Olajide",
                player_score = "10",
                code = "HTESXESS",
                topic_id = "1",
            )
        )

        GameSummaryContent(participants = listParticipant, navHostController = navController)
//        DashboardScreen(navHostController = navController, token = "")
//        LobbyScreen(navController= navController, token= "")
    }
}