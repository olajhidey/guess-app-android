package com.taloscore.guessapp.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.taloscore.guessapp.CreateGameScreen
import com.taloscore.guessapp.GameInfo
import com.taloscore.guessapp.NewGameState
import com.taloscore.guessapp.OutlinedInputText
import com.taloscore.guessapp.R
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.model.CategoryResponse
import com.taloscore.guessapp.data.model.TopicResponse
import com.taloscore.guessapp.generateCode
import com.taloscore.guessapp.utils.Constant
import com.taloscore.guessapp.utils.TokenManager
import com.taloscore.guessapp.viewmodel.GameViewModel
import kotlinx.coroutines.launch

private const val TAG = "DashboardView"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    token: String?
) {

    BackHandler(enabled = false) {  }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val username = TokenManager.readValueInStore(context = context, Constant.USERNAME_KEY)
        .collectAsState(initial = "")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                ),
                title = {
                    Text("Dashboard")
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            TokenManager.writeToken(context, "")
                        }
                        navHostController.navigate(AppScreen.AuthScreen.route)
                    }) {
                        Icon(
                            painterResource(R.drawable.outline_logout_24),
                            null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        DashboardContent(
            modifier = Modifier.padding(innerPadding),
            token = token,
            navController = navHostController
        )
    }
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = hiltViewModel(),
    token: String?,
    navController: NavHostController
) {

    val context = LocalContext.current
    val categoryList by gameViewModel.categories.collectAsState()
    val topicList by gameViewModel.topics.collectAsState()
    val listOfCategories = remember { mutableStateListOf<CategoryResponse>() }
    val listOfTopics = remember { mutableStateListOf<TopicResponse>() }
    var category by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var gameCode by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token != null && token != ""){
            token.let { gameViewModel.categoryList(it) }
        }
    }

    LaunchedEffect(categoryList) {
        if (categoryList is ApiState.Success) {
            val data = (categoryList as ApiState.Success<List<CategoryResponse>>).data
            listOfCategories.clear()
            listOfCategories.addAll(data)
        }

        if (categoryList is ApiState.Error) {
            val err = (categoryList as ApiState.Error).message
            if (err.contains("token")) {
                TokenManager.writeToken(context, "")
                navController.navigate(AppScreen.AuthScreen.route)
            }
        }
    }

    LaunchedEffect(topicList) {
        if (topicList is ApiState.Success) {
            val data = (topicList as ApiState.Success<List<TopicResponse>>).data
            listOfTopics.clear()
            listOfTopics.addAll(data)
        }

        if (topicList is ApiState.Error) {
            val err = (topicList as ApiState.Error).message
            Log.e(TAG, "Error fetching topic: $err")
        }
    }

    var joinCode by remember { mutableStateOf("") }
    val username =
        TokenManager.readValueInStore(context, Constant.USERNAME_KEY).collectAsState(initial = "")

    Column(modifier = modifier.fillMaxSize()) {

        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp),
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome back, ${username.value}!",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    OutlinedInputText(
                        label = "Enter Code",
                        inputValue = joinCode,
                        onValueChange = { joinCode = it },
                        modifier = Modifier.padding(top = 16.dp),
                        isPassword = false
                    )
                    Button(
                        onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set(
                                "gameInfo",
                                GameInfo(
                                    gameCode = joinCode,
                                    isHost = false,
                                    topicId = "",
                                    categoryId = "",
                                    username = username.value
                                )
                            )
                            navController.navigate(AppScreen.LobbyScreen.route)
                        },
                        enabled = joinCode != "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Join Game")
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            shadowElevation = 4.dp
        ) {
            val gameData = NewGameState(
                category = category,
                topic = topic,
                code = gameCode
            )
            CreateGameScreen(
                categories = listOfCategories,
                topics = listOfTopics,
                newGame = gameData,
                onCategoryChange = { selectedCategory ->
                    category = selectedCategory
                    Log.e(TAG, "Category selected $selectedCategory")
                    token?.let { gameViewModel.topicList(selectedCategory, it) }
                },
                onStartGame = {
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "gameInfo",
                        GameInfo(gameCode, true, topic, category, username.value)
                    )
                    navController.navigate(AppScreen.LobbyScreen.route)
                },
                onGenerateGameCode = {
                    gameCode = generateCode()
                },
                onTopicChange = {
                    topic = it
                },
            )
        }

    }
}
