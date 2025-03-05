package com.andiasrafil.asahaitest.feature.main.ui
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieAnimationState
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.andiasrafil.asahaitest.helpers.enums.Voice
import com.andiasrafil.asahaitest.helpers.enums.VoiceGender
import com.andiasrafil.asahaitest.ui.components.NetworkImage
import com.andiasrafil.asahaitest.ui.theme.AsahAITestTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsahAITestTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val mainUIState by mainViewModel.uiState.collectAsState()

    LaunchedEffect(mainUIState.errorMessage) {
        mainUIState.errorMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message)
                //mainViewModel.clearErrorMessage()
            }
        }
    }

    DisposableEffect(true) {
        onDispose {
            mainViewModel.releasePlayer()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            MainContent(mainViewModel, mainUIState)

            Box {
                NextButton(
                    modifier = Modifier,
                    enabled = mainUIState.selectedVoice != null,
                    onClick = {
                        mainViewModel.onNextTap()
                    })
            }
        }
    }

}

@Composable
fun MainContent(mainViewModel: MainViewModel, mainUIState: MainUIState) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .zIndex(1f)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageTitle()
            if (mainUIState.isLoading) {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LottieAsset(mainUIState.iteration, observe = { animationState ->
                    if (animationState.isAtEnd) {
                        mainViewModel.updateTap()
                    }
                })
            }
            VoicesGrid(
                selectedVoice = mainUIState.selectedVoice,
                onClick = { voice ->
                    mainViewModel.onVoiceCardClick(voice = voice)
                }
            )
        }
    }
}

@Composable
fun PageTitle() {
    Text("Pick My Voices", fontSize = 28.sp)
}

@Composable
fun NextButton(modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (enabled) listOf(
                            Color(0xFFCC5500),
                            Color(0xFFCC5500).copy(alpha = 0.5f),
                        ) else listOf(
                            Color.Gray,
                            Color.Gray
                        )

                    ), shape = ButtonDefaults.shape
                )
                .clip(RoundedCornerShape(16.dp))
        ) {
            Text("Next")
        }
    }
}

@Composable
fun LottieAsset(iteration: Int, observe: (LottieAnimationState) -> Unit) {
    val isPlaying = iteration != 999

    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url("https://static.dailyfriend.ai/images/mascot-animation.json")
    )
    val animationState = animateLottieCompositionAsState(
        composition,
        isPlaying = isPlaying,
        iterations = iteration
    )

    observe(animationState)

    LottieAnimation(
        composition = composition,
        progress = { animationState.progress },
        modifier = Modifier
            .size(200.dp)
            .scale(2.5f),
    )
}

@Composable
fun VoicesGrid(onClick: (Voice) -> Unit, selectedVoice: Voice?) {
    val voices = Voice.entries.toTypedArray()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .heightIn(max = 1000.dp)
            .padding(bottom = 40.dp)
    ) {
        items(voices) { voice ->
            VoiceCard(
                voice, onClick = {
                    onClick(voice)
                },
                isSelected = voice == selectedVoice
            )
        }
    }
}

@Composable
fun VoiceCard(voice: Voice, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (voice.voiceGender == VoiceGender.male)
                Color(0xFFF2D2BD)
            else Color(0xFFE97451)
        ),
        border = BorderStroke(
            1.dp,
            color = if (isSelected) Color.Blue else Color.Transparent
        ),
        modifier = Modifier
            .width(150.dp)
            .clickable(
                enabled = true,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(voice.displayName)
                RadioButton(
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Blue, unselectedColor = Color.Gray),
                    selected = isSelected,
                    onClick = onClick
                )
            }
            NetworkImage(
                modifier = Modifier.fillMaxWidth(),
                url = voice.getImageUrl()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MainScreen()
}