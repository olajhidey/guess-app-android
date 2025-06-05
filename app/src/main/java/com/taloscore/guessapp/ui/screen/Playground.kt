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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.taloscore.guessapp.OptionButton
import com.taloscore.guessapp.viewmodel.Question
import com.taloscore.guessapp.viewmodel.WebsocketViewModel
import kotlinx.coroutines.delay

private const val TAG = "PlaygroundScreen"
@Composable
fun PlayGroundScreen(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    websocketViewModel: WebsocketViewModel
) {

    BackHandler(enabled = false) {  }

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
