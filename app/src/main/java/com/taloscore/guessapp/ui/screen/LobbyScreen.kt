package com.taloscore.guessapp.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.taloscore.guessapp.GameInfo
import com.taloscore.guessapp.viewmodel.WebsocketViewModel

private const val TAG = "LobbyScreen"
@Composable
fun LobbyScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    token: String?,
    websocketViewModel: WebsocketViewModel
) {

    BackHandler(
        enabled = false
    ) {  }

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
