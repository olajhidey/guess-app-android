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
fun LobbyScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    token: String?,
    websocketViewModel: WebsocketViewModel
) {

    val connectStatus by websocketViewModel.isConnected.collectAsState()
    var connected by remember { mutableStateOf(false) }

    LaunchedEffect(connectStatus) {
        if (connectStatus) {
            connected = true
        }
    }

    val gameInfo: GameInfo? = remember {
        navController
            .previousBackStackEntry
            ?.savedStateHandle
            ?.get<GameInfo>("gameInfo")
    }

    LaunchedEffect(Unit) {
        websocketViewModel.connect(token!!, gameInfo!!, navController)
    }

    LaunchedEffect(Unit) {
        Log.e(TAG, "LobbyScreen: ${gameInfo}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Game Lobby",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Game Code: ", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = gameInfo!!.gameCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
//                    Text(
//                        text = timer,
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.padding(top = 8.dp)
//                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Waiting message
                Text(
                    text = "Waiting for players to join...",
                    style = MaterialTheme.typography.titleMedium
                )

//                // User list
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 12.dp)
//                        .heightIn(max = 300.dp)
//                ) {
//                    itemsIndexed(users) { index, user ->
//                        Surface(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 4.dp),
//                            shape = RoundedCornerShape(6.dp),
//                            shadowElevation = 2.dp,
//                            color = MaterialTheme.colorScheme.surfaceVariant
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(horizontal = 12.dp, vertical = 8.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(text = gameInfo!!.username, style = MaterialTheme.typography.bodyLarge)
//                            }
//                        }
//                    }
//                }

                // Start Game button (only for host)
                if (gameInfo?.isHost!!) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            websocketViewModel.startGame(token!!, gameInfo)
                            navController.navigate(AppScreen.PlayGroundScreen.route)
                        },
                        enabled = connected,
                        colors = buttonColors(containerColor = Color(0xFF198754)),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Start Game")
                    }
                }
            }
        }
    }
}

@Composable
fun PlayGroundScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    websocketViewModel: WebsocketViewModel
) {

    val question by websocketViewModel.question.collectAsState()
    var questionData by remember { mutableStateOf<Question?>(null) }
    var questionNumber by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var playerScore by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(0) }

    LaunchedEffect(question) {
        if (question != null) {
            questionNumber++
            selectedOption = ""
            questionData = question
            timeLeft = 5
            Log.e(TAG, "PlayGroundScreen: ${question}")
            websocketViewModel.onTopicChange(question!!.topic_id)
        }
    }

    LaunchedEffect(timeLeft) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Question $questionNumber",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        color = Color(0xFFFFC107),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "\u23F3 ${timeLeft}s",
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Question Image
                question?.image_url?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Question Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Options
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        questionData?.option1.let { option ->
                            OptionButton(
                                option.toString(),
                                selectedOption,
                                isCorrect,
                                {
                                    selectedOption = it
                                    isCorrect = option == questionData?.answer
                                    if (isCorrect) {
                                        playerScore += 10
                                        websocketViewModel.onScoreChange(playerScore)
                                    }
                                })
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        question?.option2?.let { option ->
                            OptionButton(option, selectedOption, isCorrect, {
                                selectedOption = it
                                isCorrect = option == questionData?.answer
                                if (isCorrect) {
                                    playerScore += 10
                                }
                            })
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        question?.option3?.let { option ->
                            OptionButton(option, selectedOption, isCorrect, {
                                selectedOption = it
                                isCorrect = option == questionData?.answer
                                if (isCorrect) {
                                    playerScore += 10
                                }
                            })
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        question?.option4?.let { option ->
                            OptionButton(option, selectedOption, isCorrect, {
                                selectedOption = it
                                isCorrect = option == questionData?.answer
                                if (isCorrect) {
                                    playerScore += 10
                                }
                            })
                        }
                    }
                }

                // Feedback
                selectedOption.let {
                    Spacer(modifier = Modifier.height(24.dp))
                    if (selectedOption != "") {
                        Surface(
                            color = if (isCorrect == true) Color(0xFFD4EDDA) else Color(0xFFF8D7DA),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isCorrect == true) "Correct!" else "Oops, try again.",
                                color = if (isCorrect == true) Color(0xFF155724) else Color(
                                    0xFF721C24
                                ),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishedScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    websocketViewModel: WebsocketViewModel
) {
    BackHandler(enabled = false) {}
    val info =
        navHostController.currentBackStackEntry?.savedStateHandle?.get<FinishedData>("finishedData")
    val participants by websocketViewModel.participants.collectAsState()
    val listParticipants = remember { mutableStateListOf<Result>() }
    val loadingProgress = remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(5) }

    LaunchedEffect(Unit) {
        info?.let { data ->
            websocketViewModel.listParticipants(data.token, data.gameCode)
        }
    }

    LaunchedEffect(reload) {
        if (reload > 0) {
            delay(1000)
            reload--
        } else {
            info?.let { data ->
                websocketViewModel.listParticipants(data.token, data.gameCode)
            }
        }
    }

    LaunchedEffect(participants) {
        if (participants is ApiState.Loading) {
            Log.e(TAG, "Loading...")
            loadingProgress.value = true
        }
        if (participants is ApiState.Success) {
            val data = (participants as ApiState.Success<List<Result>>).data
            loadingProgress.value = false
            listParticipants.clear()
            listParticipants.addAll(data)
        }
        if (participants is ApiState.Error) {
            loadingProgress.value = false
            val err = (participants as ApiState.Error).message
            Log.e(TAG, "Error fetching participants: $err")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                ),
                title = {
                    Text("Game Summary")
                })
        }
    ) { innerPadding ->
        GameSummaryContent(modifier.padding(innerPadding),navHostController= navHostController, participants = listParticipants)
    }
}

@Composable
fun GameSummaryContent(modifier: Modifier = Modifier, navHostController: NavHostController, participants: List<Result>) {

    var code by remember { mutableStateOf("") }

    LaunchedEffect(participants) {
        if (participants.size > 0){
            code = participants[0].code
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
            textAlign = TextAlign.Center,
            text = "Game Ended",
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
            textAlign = TextAlign.Center,
            text = "Game code: ${code}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = {
                navHostController.navigate(AppScreen.DashboardScreen.route)
            },
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
        ) {
            Text("Play Again")
        }
        
        Card(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ){
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                LazyColumn {
                    items (participants) {participant ->
                        Row(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                        ){
                            Text(
                                text = participant.player_name,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )

                            Text(
                                text = participant.player_score,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        if (participants.indexOf(participant) != participants.size - 1){
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.LightGray)
                            )
                        }
                    }
                }

            }
        }

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