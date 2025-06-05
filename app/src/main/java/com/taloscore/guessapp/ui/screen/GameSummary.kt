package com.taloscore.guessapp.ui.screen

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.viewmodel.FinishedData
import com.taloscore.guessapp.viewmodel.Result
import com.taloscore.guessapp.viewmodel.WebsocketViewModel
import kotlinx.coroutines.delay

private const val TAG = "GameSummary"
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
    val listParticipants = remember { mutableStateListOf<com.taloscore.guessapp.viewmodel.Result>() }
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
            val data = (participants as ApiState.Success<List<com.taloscore.guessapp.viewmodel.Result>>).data
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

    BackHandler(enabled = false) {  }

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            textAlign = TextAlign.Center,
            text = "Game Ended",
            color = Color.Black,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
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

