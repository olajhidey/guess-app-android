package com.taloscore.guessapp.viewmodel

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.taloscore.guessapp.GameInfo
import com.taloscore.guessapp.Topic
import com.taloscore.guessapp.data.model.ApiState
import com.taloscore.guessapp.data.repository.CategoryRepository
import com.taloscore.guessapp.ui.screen.AppScreen
import com.taloscore.guessapp.utils.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.net.URI
import javax.inject.Inject

private const val TAG = "WebsocketViewModel"

data class User(
    val name: String,
    val gameCode: String,
    val isHost: Boolean,
    val topic: String,
    val token: String
)

data class Guest(
    val name: String,
    val gameCode: String,
    val isHost: Boolean
)

data class Question(
    val ID: Int,
    val image_url: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val answer: String,
    val topic_id: String
)

data class Result(
    val topic_id: String,
    val code: String,
    val player_score: String,
    val player_name: String
)

@Parcelize
data class FinishedData(
    val gameCode: String,
    val token: String
):Parcelable

data class EndData(
    val name: String,
    val gameCode: String,
)

@HiltViewModel
class WebsocketViewModel @Inject constructor(private val categoryRepository: CategoryRepository) :
    ViewModel() {

    private val _gameInfo = MutableStateFlow<GameInfo?>(null)
    val gameInfo = _gameInfo

    private val _participants = MutableStateFlow<ApiState<List<Result>>?>(null)
    val participants = _participants

    private val _score = MutableStateFlow<Int>(0)
    val score = _score

    private val _isConnected = MutableStateFlow<Boolean>(false)
    val isConnected = _isConnected

    private val _question = MutableStateFlow<Question?>(null)
    val question = _question

    private val _topic = MutableStateFlow<String>("")
    val topic = _topic

    private lateinit var socket: Socket

    fun connect(token: String, gameInfo: GameInfo, navHostController: NavHostController) {
        try {
            val socketOptions = IO.Options()
            socketOptions.reconnection = true
            socketOptions.forceNew = true
            socketOptions.secure = true

            socket = IO.socket(URI.create(Constant.WEBSOCKET_URL), socketOptions)
            socket.connect()
            observeConnection(token, gameInfo, navHostController)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun onScoreChange(score: Int) {
        _score.value = score
    }

    fun onTopicChange(topic: String) {
        _topic.value = topic
    }

    private fun observeConnection(
        token: String,
        gameInfo: GameInfo,
        navHostController: NavHostController
    ) {
        socket.on(Socket.EVENT_CONNECT) {
            _isConnected.value = true
            _gameInfo.value = gameInfo

            Log.e(TAG, "Connected to the Server")
            val newUser = User(
                name = gameInfo.username,
                gameCode = gameInfo.gameCode,
                isHost = gameInfo.isHost,
                topic = gameInfo.topicId,
                token = token
            )
            socket.emit("joined", Gson().toJson(newUser))
        }

        socket.on("guest added") { args ->
            Log.d(TAG, "Data: ${args[0]}")
            viewModelScope.launch {
                navHostController.navigate(AppScreen.PlayGroundScreen.route)
            }
        }

        socket.on("question") { args ->
            Log.d(TAG, "Question: ${args[0]}")
            val response = Gson().fromJson(args[0].toString(), Question::class.java)
            _question.value = response
        }

        socket.on("end") { args ->
            Log.d(TAG, "Game Ends: ${args[0]}")
            val result = Result(
                topic_id = if (gameInfo.topicId == "") _topic.value else gameInfo.topicId,
                player_score = _score.value.toString(),
                player_name = gameInfo.username,
                code = gameInfo.gameCode
            )
            Log.e(TAG, "Result: $result")
            disconnectUser(gameInfo.username, gameInfo.gameCode)
            viewModelScope.launch {
                categoryRepository.createGame(token, result).collect {
                    if (it is ApiState.Success) {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set("finishedData", FinishedData(gameInfo.gameCode, token))
                        navHostController.navigate(AppScreen.FinishedScreen.route)
                        listParticipants(token, gameInfo.gameCode)
                    }
                    else if (it is ApiState.Error) {
                        Log.e(TAG, "Error: ${it.message}")
                    }
                }

            }
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            _isConnected.value = false
            Log.e(TAG, "Disconnected from the Server")
        }
    }

    fun startGame(token: String, gameInfo: GameInfo) {
        Log.e(TAG, "Starting game....")
        val newUser = User(
            name = gameInfo.username,
            gameCode = gameInfo.gameCode,
            isHost = gameInfo.isHost,
            topic = gameInfo.topicId,
            token = token
        )
        socket.emit("start game", Gson().toJson(newUser))
        socket.emit("add guest", Gson().toJson(newUser))
    }

    fun listParticipants(token: String, gameCode: String) {
        viewModelScope.launch {
            _participants.value = ApiState.Loading
            categoryRepository.listParticipants(token, gameCode).collect {
                _participants.value = it
            }
        }
    }

    fun disconnectUser(name: String, gameCode: String){
        socket.emit("leave", Gson().toJson(EndData(name, gameCode)))
        socket.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        socket.disconnect()
    }

}