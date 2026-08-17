package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.AnimalEntity
import com.example.data.FamilyEntity
import com.example.ui.audio.YuliAudioSynth
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.YuliAppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

// --- Top Level Data Classes ---
data class Song(val name: String, val notes: List<String>)
data class Wash(val color: Color, val base: Offset, val radius: Float)
data class Blob(val color: Color, val base: Offset, val radius: Float)
data class FloatingItem(val emoji: String, val base: Offset, val duration: Int)
data class WatercolorSpot(val center: Offset, val radius: Float, val alpha: Float)
data class QuizQuestion(val targetShape: String, val targetColorName: String, val targetColor: Color, val options: List<Pair<String, String>>)
data class InstrumentBubble(val id: Long, val text: String, val color: Color, val initialX: Float, val initialY: Float, val randomOffsetX: Float)
data class RandomParticle(val emoji: String, val startX: Float, val duration: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: YuliAppViewModel = viewModel()
                YuliApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun YuliApp(viewModel: YuliAppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isParentMode by viewModel.isParentMode.collectAsState()
    val isAudioEnabled by viewModel.isAudioEnabled.collectAsState()
    val backgroundVideoPath by viewModel.backgroundVideoPath.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()

    if (showTutorial) {
        TutorialDialog(onDismiss = { viewModel.showTutorial(false) })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen != "home") {
                YuliBottomBar(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        YuliAudioSynth.playNavigationTo(screen)
                        viewModel.setScreen(screen)
                    },
                    modifier = Modifier.testTag("yuli_bottom_bar")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (backgroundVideoPath != null) {
                VideoBackgroundContainer(videoPath = backgroundVideoPath!!)
            } else {
                MagicBackground()
            }

            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isParentMode,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f)
                ) {
                    ParentModeBanner(
                        onLockAgain = {
                            YuliAudioSynth.playPop()
                            viewModel.toggleParentMode()
                        },
                        onOptions = {
                            viewModel.setScreen("options")
                        }
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "home" -> HomeScreen(viewModel = viewModel)
                        "colors" -> ColorsScreen(viewModel = viewModel)
                        "animals" -> AnimalsScreen(viewModel = viewModel)
                        "piano" -> PianoScreen(viewModel = viewModel)
                        "family" -> FamilyScreen(viewModel = viewModel)
                        "drawing" -> DrawingScreen(viewModel = viewModel)
                        "options" -> OptionsScreen(viewModel = viewModel)
                        "legal" -> LegalScreen(onBack = { viewModel.setScreen("options") })
                    }
                    
                    // Global Volume Button on HomeScreen
                    if (currentScreen == "home") {
                        MagicVolumeButton(
                            isEnabled = isAudioEnabled,
                            onToggle = { viewModel.toggleAudio() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MagicVolumeButton(isEnabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    var isPressed by remember { mutableStateOf(false) }
    val offset by animateDpAsState(if (isPressed) 4.dp else 0.dp)

    Box(
        modifier = modifier
            .size(80.dp)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    isPressed = true
                    try {
                        awaitRelease()
                        onToggle()
                    } finally {
                        isPressed = false
                    }
                })
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .offset(y = 8.dp)
                .fillMaxSize()
                .background(Color(0xFFDADADA), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(y = offset)
                .fillMaxSize()
                .background(Color.White, CircleShape)
                .border(6.dp, Color(0xFFEEEEEE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isEnabled) com.example.ui.theme.MagicSecondary else Color.Gray
            )
        }
    }
}

@Composable
fun MagicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "magic_bg")
    val cloudDrift1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "cloud1"
    )
    val cloudDrift2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "cloud2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Sky
        drawRect(color = com.example.ui.theme.MagicSky)

        // Sun
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(com.example.ui.theme.MagicSun, com.example.ui.theme.MagicSunGlow, Color.Transparent),
                center = Offset(80.dp.toPx(), 80.dp.toPx()),
                radius = 120.dp.toPx()
            ),
            center = Offset(80.dp.toPx(), 80.dp.toPx()),
            radius = 120.dp.toPx()
        )
        drawCircle(
            color = com.example.ui.theme.MagicSun,
            center = Offset(80.dp.toPx(), 80.dp.toPx()),
            radius = 60.dp.toPx()
        )

        // Clouds
        drawCloud(Offset(size.width * 0.25f + cloudDrift1.dp.toPx(), size.height * 0.2f), 80.dp.toPx())
        drawCloud(Offset(size.width * 0.75f + cloudDrift2.dp.toPx(), size.height * 0.15f), 60.dp.toPx())
        drawCloud(Offset(size.width * 0.6f + cloudDrift1.dp.toPx() * 0.5f, size.height * 0.35f), 90.dp.toPx())

        // Horizon / Ground
        val groundPath = Path().apply {
            moveTo(-100f, size.height * 0.85f)
            quadraticTo(size.width / 2, size.height * 0.75f, size.width + 100f, size.height * 0.85f)
            lineTo(size.width + 100f, size.height + 100f)
            lineTo(-100f, size.height + 100f)
            close()
        }
        drawPath(
            path = groundPath,
            color = Color(0xFFEEEEEE)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(center: Offset, width: Float) {
    val cloudColor = Color.White.copy(alpha = 0.7f)
    drawCircle(cloudColor, radius = width * 0.4f, center = center)
    drawCircle(cloudColor, radius = width * 0.3f, center = center.copy(x = center.x - width * 0.3f, y = center.y + width * 0.1f))
    drawCircle(cloudColor, radius = width * 0.3f, center = center.copy(x = center.x + width * 0.3f, y = center.y + width * 0.1f))
}

@Composable
fun MagicButton(
    title: String,
    emoji: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    backgroundColor: Color,
    shadowColor: Color,
    textColor: Color,
    size: androidx.compose.ui.unit.Dp = 180.dp,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val shadowOffset by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 12.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "shadow"
    )
    val buttonOffset by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .size(size + 12.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                            onClick()
                        } finally {
                            isPressed = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .offset(y = 12.dp)
                .size(size)
                .background(shadowColor, CircleShape)
        )
        // Button layer
        Box(
            modifier = Modifier
                .offset(y = buttonOffset)
                .size(size)
                .background(backgroundColor, CircleShape)
                .border(8.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.size(size * 0.4f), tint = textColor)
                } else {
                    Text(emoji, fontSize = (size.value * 0.35).sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    title,
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.1).sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingIcon(item: FloatingItem, screenWidth: androidx.compose.ui.unit.Dp, screenHeight: androidx.compose.ui.unit.Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating_icon")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(item.duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y_offset"
    )
    val xOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(item.duration + 2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = screenWidth * item.base.x + xOffset.dp,
                y = screenHeight * item.base.y + yOffset.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = item.emoji,
            fontSize = 44.sp,
            modifier = Modifier
                .blur(0.5.dp)
                .graphicsLayer(alpha = 0.35f)
        )
    }
}

@Composable
fun CategoryBubble(
    title: String,
    emoji: String,
    color: Color,
    testTag: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bubble_scale"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(10.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .border(
                width = 6.dp,
                color = Color.White,
                shape = RoundedCornerShape(32.dp)
            )
            .testTag(testTag)
            .clickable(
                onClick = {
                    isPressed = true
                    YuliAudioSynth.playForwardNavigation()
                    onClick()
                }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Sticker effect: white glow behind emoji
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 48.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(2f, 2f),
                            blurRadius = 2f
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun ConfettiCelebration(isVisible: Boolean) {
    if (!isVisible) return
    val particles = remember {
        List(40) {
            RandomParticle(
                emoji = listOf("✨", "🌟", "🎈", "🍭", "🎨", "🎉", "🍬", "🦋").random(),
                startX = Random().nextFloat(),
                duration = 1500 + Random().nextInt(1500)
            )
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = this.maxWidth
        val h = this.maxHeight
        particles.forEach { particle ->
            ParticleEffect(particle, w, h)
        }
    }
}

@Composable
fun ParticleEffect(particle: RandomParticle, screenWidth: androidx.compose.ui.unit.Dp, screenHeight: androidx.compose.ui.unit.Dp) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, animationSpec = tween(particle.duration, easing = LinearOutSlowInEasing))
    }

    if (anim.value < 1f) {
        Text(
            text = particle.emoji,
            modifier = Modifier
                .offset(
                    x = screenWidth * particle.startX,
                    y = screenHeight * (1f - anim.value)
                )
                .graphicsLayer(
                    alpha = 1f - anim.value,
                    scaleX = 0.5f + anim.value,
                    scaleY = 0.5f + anim.value,
                    rotationZ = anim.value * 360f
                ),
            fontSize = 32.sp
        )
    }
}

@Composable
fun ColorsScreen(viewModel: YuliAppViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val colorsMap = remember {
        mapOf(
            "Rojo" to com.example.ui.theme.ToddleRed,
            "Amarillo" to com.example.ui.theme.ToddleYellow,
            "Azul" to com.example.ui.theme.ToddleBlue,
            "Verde" to com.example.ui.theme.ToddleGreen,
            "Naranja" to com.example.ui.theme.ToddleOrange,
            "Morado" to com.example.ui.theme.ToddlePurple
        )
    }

    val colorsList = remember {
        listOf(
            Triple("ROJO", com.example.ui.theme.ToddleRed, "❤️"),
            Triple("AMARILLO", com.example.ui.theme.ToddleYellow, "💛"),
            Triple("AZUL", com.example.ui.theme.ToddleBlue, "💙"),
            Triple("VERDE", com.example.ui.theme.ToddleGreen, "💚"),
            Triple("NARANJA", com.example.ui.theme.ToddleOrange, "🧡"),
            Triple("MORADO", com.example.ui.theme.ToddlePurple, "💜")
        )
    }

    var isQuizMode by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var feedbackMessage by remember { mutableStateOf("") }
    var showSuccessCelebration by remember { mutableStateOf(false) }

    fun generateNewQuestion() {
        val shapes = listOf("Círculo", "Cuadrado", "Triángulo", "Estrella")
        val colorNames = colorsMap.keys.toList()
        val pool = mutableListOf<Pair<String, String>>()
        for (s in shapes) {
            for (c in colorNames) {
                pool.add(Pair(s, c))
            }
        }
        pool.shuffle()
        val options = pool.take(4)
        val target = options.random()
        currentQuestion = QuizQuestion(
            targetShape = target.first,
            targetColorName = target.second,
            targetColor = colorsMap[target.second] ?: Color.Red,
            options = options
        )
        feedbackMessage = "¡Busca el ${target.first} ${target.second}!"
        showSuccessCelebration = false
    }

    LaunchedEffect(isQuizMode) {
        if (isQuizMode) {
            generateNewQuestion()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        YuliAudioSynth.playBackNavigation()
                        viewModel.setScreen("home")
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White, CircleShape)
                        .shadow(3.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Ir al inicio",
                        tint = Color(0xFFFF4B4B),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Button(
                    onClick = {
                        YuliAudioSynth.playPop()
                        isQuizMode = !isQuizMode
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isQuizMode) Color(0xFFFF4B4B) else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isQuizMode) "🧩 MODO LIBRE" else "🎮 MODO JUEGO",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isQuizMode) {
                Text(
                    text = "¡TOCA UN COLOR!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = com.example.ui.theme.ToddleBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(colorsList) { item ->
                        ColorBubbleItem(item = item, haptic = haptic)
                    }
                }
            } else {
                currentQuestion?.let { question ->
                    Text(
                        text = feedbackMessage,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 60.dp)
                    ) {
                        items(question.options) { option ->
                            QuizOptionItem(
                                option = option,
                                question = question,
                                colorsMap = colorsMap,
                                onCorrect = {
                                    feedbackMessage = "🎉 ¡EXCELENTE!"
                                    showSuccessCelebration = true
                                    YuliAudioSynth.playTone(392.00, 180, "sine", 523.25)
                                    coroutineScope.launch {
                                        delay(2000)
                                        generateNewQuestion()
                                    }
                                },
                                onWrong = {
                                    feedbackMessage = "¡Inténtalo de nuevo! 😊"
                                    YuliAudioSynth.playTone(220.0, 180, "sine", 160.0)
                                }
                            )
                        }
                    }
                }
            }
        }
        ConfettiCelebration(isVisible = showSuccessCelebration)
    }
}

@Composable
fun ColorBubbleItem(item: Triple<String, Color, String>, haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(36.dp),
                spotColor = item.second.copy(alpha = 0.8f)
            )
            .clip(RoundedCornerShape(36.dp))
            .background(item.second)
            .clickable {
                isPressed = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val freq = when (item.first) {
                    "ROJO" -> 261.63
                    "NARANJA" -> 293.66
                    "AMARILLO" -> 329.63
                    "VERDE" -> 392.00
                    "AZUL" -> 440.00
                    "MORADO" -> 523.25
                    else -> 392.00
                }
                YuliAudioSynth.playMarimbaNote(freq, 350)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = item.third, fontSize = 52.sp)
            Text(
                text = item.first,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.first == "AMARILLO") Color(0xFF5D4037) else Color.White
            )
        }
    }
}

@Composable
fun QuizOptionItem(
    option: Pair<String, String>,
    question: QuizQuestion,
    colorsMap: Map<String, Color>,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val shapeName = option.first
    val colorName = option.second
    val shapeColor = colorsMap[colorName] ?: Color.Gray
    val isTarget = shapeName == question.targetShape && colorName == question.targetColorName

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(120)
            isPressed = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable {
                isPressed = true
                if (isTarget) onCorrect() else onWrong()
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            SensoryShapeItem(
                shape = shapeName,
                color = shapeColor,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$shapeName\n$colorName",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF37474F),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SensoryShapeItem(shape: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        when (shape) {
            "Círculo" -> drawCircle(color = color, radius = size.minDimension * 0.42f)
            "Cuadrado" -> drawRoundRect(
                color = color,
                size = Size(width * 0.8f, height * 0.8f),
                topLeft = Offset(width * 0.1f, height * 0.1f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            "Triángulo" -> {
                val path = Path().apply {
                    moveTo(width / 2f, height * 0.15f)
                    lineTo(width * 0.85f, height * 0.85f)
                    lineTo(width * 0.15f, height * 0.85f)
                    close()
                }
                drawPath(path = path, color = color)
            }
            "Estrella" -> {
                val path = Path()
                val centerX = width / 2f
                val centerY = height / 2f
                val r = minOf(width, height) / 2f * 0.85f
                val innerR = r * 0.42f
                for (i in 0 until 10) {
                    val angle = i * Math.PI / 5 - Math.PI / 2
                    val currR = if (i % 2 == 0) r else innerR
                    val x = centerX + currR * Math.cos(angle).toFloat()
                    val y = centerY + currR * Math.sin(angle).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path = path, color = color)
            }
        }
    }
}

@Composable
fun VideoBackgroundContainer(videoPath: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoPath(videoPath)
                    setOnPreparedListener { mediaPlayer ->
                        mediaPlayer.isLooping = true
                        mediaPlayer.setVolume(0f, 0f)
                        start()
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Semi-transparent overlay to keep UI readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.3f))
        )
        // Add Magic Horizon to keep some consistency
        Canvas(modifier = Modifier.fillMaxSize()) {
            val groundPath = Path().apply {
                moveTo(-100f, size.height * 0.85f)
                quadraticTo(size.width / 2, size.height * 0.75f, size.width + 100f, size.height * 0.85f)
                lineTo(size.width + 100f, size.height + 100f)
                lineTo(-100f, size.height + 100f)
                close()
            }
            drawPath(
                path = groundPath,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WatercolorSplashes(color: Color, count: Int, seed: Int, modifier: Modifier = Modifier) {
    val spots = remember(color, count, seed) {
        val random = Random(seed.toLong())
        List(count) {
            WatercolorSpot(
                center = Offset(random.nextFloat(), random.nextFloat()),
                radius = 0.22f + random.nextFloat() * 0.26f,
                alpha = 0.08f + random.nextFloat() * 0.10f
            )
        }
    }
    Canvas(modifier = modifier) {
        spots.forEach { spot ->
            val center = Offset(size.width * spot.center.x, size.height * spot.center.y)
            val radius = size.maxDimension * spot.radius
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = spot.alpha), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun ParentModeBanner(onLockAgain: () -> Unit, onOptions: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0))
            .statusBarsPadding()
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOptions) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "AJUSTES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                modifier = Modifier.clickable { onOptions() }
            )
        }
        
        Button(
            onClick = onLockAgain,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Cerrar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HomeScreen(viewModel: YuliAppViewModel) {
    val isParentMode by viewModel.isParentMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "YuliApp 🎈",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = Color.Yellow,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "¡VAMOS A JUGAR!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.ui.theme.MagicSecondary,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    MagicButton(
                        title = "Formas",
                        emoji = "🎨",
                        icon = Icons.Default.Extension,
                        backgroundColor = com.example.ui.theme.MagicSecondary,
                        shadowColor = Color(0xFF001E2D),
                        textColor = Color.White,
                        size = 120.dp
                    ) {
                        viewModel.setScreen("colors")
                    }
                    MagicButton(
                        title = "Animales",
                        emoji = "🦁",
                        icon = Icons.Default.CrueltyFree,
                        backgroundColor = com.example.ui.theme.MagicSun,
                        shadowColor = Color(0xFF544600),
                        textColor = com.example.ui.theme.MagicPrimary,
                        size = 140.dp
                    ) {
                        viewModel.setScreen("animals")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    MagicButton(
                        title = "Piano",
                        emoji = "🎹",
                        backgroundColor = com.example.ui.theme.MagicTertiary,
                        shadowColor = Color(0xFF410002),
                        textColor = Color.White,
                        size = 120.dp
                    ) {
                        viewModel.setScreen("piano")
                    }
                    MagicButton(
                        title = "Familia",
                        emoji = "👩",
                        backgroundColor = com.example.ui.theme.ToddleOrange,
                        shadowColor = Color(0xFFE65100),
                        textColor = Color.White,
                        size = 120.dp
                    ) {
                        viewModel.setScreen("family")
                    }
                }
            }
        }

        // Parent Section Integrated
        ParentLockSection(
            isParentMode = isParentMode,
            onUnlocked = { viewModel.toggleParentMode() },
            onAjustes = { viewModel.setScreen("options") },
            onDrawing = { viewModel.setScreen("drawing") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Main Drawing Button at the bottom
        MagicButton(
            title = "Dibujo Libre",
            emoji = "🖍️",
            icon = Icons.Default.Edit,
            backgroundColor = Color(0xFF4CAF50),
            shadowColor = Color(0xFF1B5E20),
            textColor = Color.White,
            size = 130.dp
        ) {
            viewModel.setScreen("drawing")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mini Tutorial Button
        Button(
            onClick = { viewModel.showTutorial(true) },
            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.MagicSun.copy(alpha = 0.9f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(Icons.Default.Lightbulb, null, tint = com.example.ui.theme.MagicPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "¿CÓMO JUGAR? 💡",
                color = com.example.ui.theme.MagicPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ParentLockSection(
    isParentMode: Boolean, 
    onUnlocked: () -> Unit,
    onAjustes: () -> Unit,
    onDrawing: () -> Unit
) {
    var holdProgress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val duration = 2000f
            val startTime = System.currentTimeMillis()
            while (isHolding && holdProgress < 1f) {
                holdProgress = ((System.currentTimeMillis() - startTime) / duration).coerceAtMost(1f)
                if (holdProgress >= 1f) {
                    onUnlocked()
                    isHolding = false
                }
                delay(16)
            }
        } else {
            while (holdProgress > 0f) {
                holdProgress = (holdProgress - 0.05f).coerceAtLeast(0f)
                delay(16)
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isParentMode) {
            // Drawing shortcut for parents
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onDrawing() },
                shape = CircleShape,
                color = com.example.ui.theme.MagicTertiary,
                shadowElevation = 8.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖍️", fontSize = 14.sp)
                        Text("DIBUJO", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Settings circle
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onAjustes() },
                shape = CircleShape,
                color = Color.LightGray,
                shadowElevation = 8.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null, tint = Color.DarkGray, modifier = Modifier.size(28.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isParentMode) "🔓 AJUSTES ACTIVOS" else "🔒 MANTÉN PARA AJUSTES",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, Offset(2f, 2f), 2f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(if (isParentMode) Color(0xFFE65100) else Color.Gray, CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            isHolding = true
                            try {
                                awaitRelease()
                            } finally {
                                isHolding = false
                            }
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color.Yellow,
                        startAngle = -90f,
                        sweepAngle = holdProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx())
                    )
                }
                Icon(
                    imageVector = if (isParentMode) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
@Composable
fun YuliBottomBar(currentScreen: String, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("🏠", "Inicio", currentScreen == "home") { onNavigate("home") }
            BottomNavItem("🎨", "Formas", currentScreen == "colors") { onNavigate("colors") }
            BottomNavItem("🐮", "Granja", currentScreen == "animals") { onNavigate("animals") }
            BottomNavItem("🎹", "Piano", currentScreen == "piano") { onNavigate("piano") }
            BottomNavItem("🖍️", "Dibujo", currentScreen == "drawing") { onNavigate("drawing") }
            BottomNavItem("👩", "Familia", currentScreen == "family") { onNavigate("family") }
        }
    }
}

@Composable
fun BottomNavItem(emoji: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) com.example.ui.theme.MagicSky.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = emoji, fontSize = 24.sp)
        if (isActive) {
            Text(
                text = label, 
                fontSize = 9.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = com.example.ui.theme.MagicSecondary
            )
        }
    }
}

// --- Animals Screen ---
@Composable
fun AnimalsScreen(viewModel: YuliAppViewModel) {
    val animals by viewModel.animals.collectAsState()
    val isParentMode by viewModel.isParentMode.collectAsState()
    val recordingEntityId by viewModel.recordingEntityId.collectAsState()
    var showAddAnimalDialog by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf<AnimalEntity?>(null) }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            showRecordDialog?.let { animal ->
                viewModel.saveImportedAudioForAnimal(animal, selectedUri)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setScreen("home") },
                    modifier = Modifier.background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Home, null, tint = Color(0xFF4CAF50))
                }
                if (isParentMode) {
                    Button(
                        onClick = { showAddAnimalDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text(" Añadir Animal")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡VAMOS A LA GRANJA!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.ui.theme.ToddleBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(animals) { animal ->
                    AnimalCard(
                        animal = animal,
                        isParentMode = isParentMode,
                        isRecording = recordingEntityId == "animal_${animal.id}",
                        onTap = { viewModel.playAnimalSound(animal) },
                        onRecord = { showRecordDialog = animal },
                        onDelete = { viewModel.deleteAnimal(animal.id) }
                    )
                }
            }
        }

        if (showAddAnimalDialog) {
            AddAnimalDialog(
                onDismiss = { showAddAnimalDialog = false },
                onConfirm = { name, emoji, color ->
                    viewModel.addAnimal(name, emoji, color)
                    showAddAnimalDialog = false
                }
            )
        }

        if (showRecordDialog != null) {
            RecordVoiceDialog(
                title = "Grabar voz para ${showRecordDialog!!.name}",
                isRecording = recordingEntityId == "animal_${showRecordDialog!!.id}",
                onStart = { viewModel.startRecording("animal_${showRecordDialog!!.id}") },
                onStop = { viewModel.stopRecordingAndSaveAnimal(showRecordDialog!!) },
                onImport = { audioLauncher.launch("audio/*") },
                onDismiss = { showRecordDialog = null }
            )
        }
    }
}

@Composable
fun AnimalCard(
    animal: AnimalEntity,
    isParentMode: Boolean,
    isRecording: Boolean,
    onTap: () -> Unit,
    onRecord: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = remember(animal.colorHex) {
        try { Color(android.graphics.Color.parseColor(animal.colorHex)) } catch (e: Exception) { Color(0xFF43A047) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 8.dp)
            .border(6.dp, Color.White, RoundedCornerShape(24.dp))
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .clickable { onTap() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // "Sticker" area
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(animal.emoji, fontSize = 80.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Label area
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = animal.name.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cardColor,
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (isParentMode) {
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                    IconButton(onClick = onRecord, modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape).size(36.dp)) {
                        Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, null, tint = if (isRecording) Color.Red else Color.Gray)
                    }
                    if (animal.synthType == "CUSTOM") {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape).size(36.dp)) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
            
            if (animal.isCustomSoundEnabled) {
                Text(
                    "🗣️",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    fontSize = 20.sp
                )
            }
        }
    }
}

// --- Family Screen ---
@Composable
fun FamilyScreen(viewModel: YuliAppViewModel) {
    val family by viewModel.family.collectAsState()
    val isParentMode by viewModel.isParentMode.collectAsState()
    val recordingEntityId by viewModel.recordingEntityId.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf<FamilyEntity?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            showRecordDialog?.let { member ->
                viewModel.savePhotoForFamily(member, selectedUri)
            }
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            showRecordDialog?.let { member ->
                viewModel.saveImportedAudioForFamily(member, selectedUri)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setScreen("home") },
                    modifier = Modifier.background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Home, null, tint = Color(0xFFFFD54F))
                }
                if (isParentMode) {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text(" Añadir Familiar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¡MI FAMILIA!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.example.ui.theme.ToddleBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(family) { member ->
                    FamilyCard(
                        member = member,
                        isParentMode = isParentMode,
                        isRecording = recordingEntityId == "family_${member.id}",
                        onTap = { viewModel.playFamilySound(member) },
                        onEdit = { showRecordDialog = member },
                        onDelete = { viewModel.deleteFamilyMember(member.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddFamilyMemberDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { relation, emoji, color ->
                    viewModel.addFamilyMember(relation, emoji, color)
                    showAddDialog = false
                }
            )
        }

        if (showRecordDialog != null) {
            RecordVoiceDialog(
                title = "Configurar a ${showRecordDialog!!.relation}",
                isRecording = recordingEntityId == "family_${showRecordDialog!!.id}",
                onStart = { viewModel.startRecording("family_${showRecordDialog!!.id}") },
                onStop = { viewModel.stopRecordingAndSaveFamily(showRecordDialog!!) },
                onImport = { audioLauncher.launch("audio/*") },
                onDismiss = { showRecordDialog = null },
                extraContent = {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { photoLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.PhotoCamera, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiar Foto")
                    }
                }
            )
        }
    }
}

@Composable
fun FamilyCard(
    member: FamilyEntity,
    isParentMode: Boolean,
    isRecording: Boolean,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = remember(member.colorHex) {
        try { Color(android.graphics.Color.parseColor(member.colorHex)) } catch (e: Exception) { Color(0xFF1E88E5) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .border(6.dp, Color.White, RoundedCornerShape(24.dp))
            .shadow(10.dp, RoundedCornerShape(24.dp))
            .clickable { onTap() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (member.customPhotoPath != null) {
                    AsyncImage(
                        model = member.customPhotoPath,
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(3.dp, Color.White, RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White.copy(alpha = 0.3f), CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(member.emoji, fontSize = 64.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = member.relation.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = cardColor,
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (isParentMode) {
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape).size(30.dp)) {
                        Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Settings, null, tint = if (isRecording) Color.Red else Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.background(Color.White.copy(alpha = 0.8f), CircleShape).size(30.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// --- Piano Screen ---
@Composable
fun PianoScreen(viewModel: YuliAppViewModel) {
    val keyCoordinatesMap = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val currentlyPressedKeys = remember { mutableStateListOf<String>() }
    val floatingBubbles = remember { mutableStateListOf<InstrumentBubble>() }
    var nextBubbleId by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val songs = remember {
        listOf(
            Song(
                "Estrellita ⭐",
                listOf(
                    "C", "C", "G", "G", "A", "A", "G",
                    "F", "F", "E", "E", "D", "D", "C",
                    "G", "G", "F", "F", "E", "E", "D",
                    "G", "G", "F", "F", "E", "E", "D",
                    "C", "C", "G", "G", "A", "A", "G",
                    "F", "F", "E", "E", "D", "D", "C"
                )
            ),
            Song(
                "Campanita 🔔",
                listOf("C", "D", "E", "C", "C", "D", "E", "C", "E", "F", "G", "E", "F", "G")
            )
        )
    }

    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var currentNoteIndex by remember { mutableIntStateOf(0) }

    val whiteKeys = listOf("C", "D", "E", "F", "G", "A", "B", "C+")
    val allKeysMap = mapOf(
        "C" to 261.63, "D" to 293.66, "E" to 329.63, "F" to 349.23,
        "G" to 392.00, "A" to 440.00, "B" to 493.88, "C+" to 523.25
    )
    val keyColors = listOf(
        Color(0xFFFFEA00), Color(0xFFCCFF00), Color(0xFFAEEA00), Color(0xFF00BFA5),
        Color(0xFF00E5FF), Color(0xFF2979FF), Color(0xFF1565C0), Color(0xFF0D47A1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setScreen("home") },
                modifier = Modifier.background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Home, null, tint = Color(0xFF0277BD))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                songs.forEach { song ->
                    Button(
                        onClick = {
                            selectedSong = if (selectedSong == song) null else song
                            currentNoteIndex = 0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSong == song) Color(0xFF4CAF50) else Color.White,
                            contentColor = if (selectedSong == song) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(song.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(com.example.ui.theme.ToddleBackground, RoundedCornerShape(24.dp))
                .border(6.dp, Color.White, RoundedCornerShape(24.dp))
                .padding(8.dp)
                .onGloballyPositioned { parentCoordinates = it }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val parentCoords = parentCoordinates ?: continue
                            val activeKeys = mutableSetOf<String>()
                            for (change in event.changes.filter { it.pressed }) {
                                for (key in whiteKeys) {
                                    val coords = keyCoordinatesMap[key]
                                    if (coords != null && parentCoords.localBoundingBoxOf(coords).contains(change.position)) {
                                        activeKeys.add(key)
                                    }
                                }
                            }
                            activeKeys.forEach { key ->
                                if (key !in currentlyPressedKeys) {
                                    YuliAudioSynth.playMarimbaNote(allKeysMap[key]!!, 300)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    // Guide logic
                                    selectedSong?.let { song ->
                                        if (key == song.notes[currentNoteIndex]) {
                                            currentNoteIndex = (currentNoteIndex + 1) % song.notes.size
                                        }
                                    }

                                    val coords = keyCoordinatesMap[key]!!
                                    val rect = parentCoords.localBoundingBoxOf(coords)
                                    val bubbleId = nextBubbleId++
                                    floatingBubbles.add(
                                        InstrumentBubble(
                                            bubbleId, "🎵", keyColors[whiteKeys.indexOf(key)],
                                            rect.center.x, rect.top, (Random().nextFloat() * 100 - 50)
                                        )
                                    )
                                    coroutineScope.launch {
                                        delay(1500)
                                        floatingBubbles.removeAll { it.id == bubbleId }
                                    }
                                }
                            }
                            currentlyPressedKeys.clear()
                            currentlyPressedKeys.addAll(activeKeys)
                        }
                    }
                }
        ) {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                whiteKeys.forEachIndexed { index, key ->
                    val isTargetNote = selectedSong?.let { it.notes[currentNoteIndex] == key } ?: false
                    val targetScale by animateFloatAsState(
                        targetValue = if (isTargetNote) 1.1f else 1.0f,
                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                        label = "target_anim"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (key in currentlyPressedKeys) 0.95f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .scale(if (isTargetNote) targetScale else scale)
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                            .background(
                                if (key in currentlyPressedKeys) keyColors[index].copy(alpha = 0.6f) 
                                else if (isTargetNote) keyColors[index].copy(alpha = 0.8f)
                                else keyColors[index]
                            )
                            .border(
                                width = if (isTargetNote) 4.dp else 0.dp,
                                color = if (isTargetNote) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                            )
                            .onGloballyPositioned { keyCoordinatesMap[key] = it },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(key, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                        if (isTargetNote) {
                            Text("🌟", modifier = Modifier.align(Alignment.Center), fontSize = 24.sp)
                        }
                    }
                }
            }
            floatingBubbles.forEach { bubble ->
                BubbleAnimationItem(bubble) { floatingBubbles.remove(bubble) }
            }
        }
    }
}

@Composable
fun BubbleAnimationItem(bubble: InstrumentBubble, onDelete: () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
        onDelete()
    }
    val progress = anim.value
    Box(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) { (bubble.initialX + progress * bubble.randomOffsetX).toDp() },
                y = with(LocalDensity.current) { (bubble.initialY - progress * 400).toDp() }
            )
            .graphicsLayer(alpha = 1f - progress, scaleX = 0.5f + progress, scaleY = 0.5f + progress),
        contentAlignment = Alignment.Center
    ) {
        Text(bubble.text, fontSize = 24.sp)
    }
}

// --- Options Screen ---
@Composable
fun OptionsScreen(viewModel: YuliAppViewModel) {
    val uriHandler = LocalUriHandler.current
    val backgroundVideoPath by viewModel.backgroundVideoPath.collectAsState()
    
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.saveBackgroundVideo(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { viewModel.setScreen("home") },
            modifier = Modifier
                .align(Alignment.Start)
                .background(Color.White, CircleShape)
        ) {
            Icon(Icons.Default.Home, null, tint = Color(0xFFAB47BC))
        }
        
        Text("⚙️ Configuración", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = com.example.ui.theme.ToddleBackground)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎨 Personalización", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.setScreen("legal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.MagicSecondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Información Legal")
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Video de Fondo:", fontSize = 14.sp)
                Text(
                    text = if (backgroundVideoPath != null) "✅ Activado" else "❌ Ninguno",
                    fontSize = 12.sp,
                    color = if (backgroundVideoPath != null) Color(0xFF4CAF50) else Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { videoLauncher.launch("video/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text("Cambiar Video", fontSize = 12.sp)
                    }
                    if (backgroundVideoPath != null) {
                        Button(
                            onClick = { viewModel.clearBackgroundVideo() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                        ) {
                            Text("Quitar", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(20.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💝 Apoya el Proyecto", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = com.example.ui.theme.MagicTertiary)
                Text(
                    text = "¡Muchas gracias por jugar con YuliApp! Si te gusta esta aplicación y deseas apoyar al desarrollador para que siga creando experiencias educativas gratuitas para los niños, puedes donar o ponerte en contacto mediante los siguientes canales:",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = { uriHandler.openUri("https://wa.me/5353466832") },
                        modifier = Modifier.size(56.dp).background(Color(0xFF25D366), CircleShape)
                    ) {
                        Icon(Icons.Default.Message, null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { uriHandler.openUri("https://www.facebook.com/reynel.busto") },
                        modifier = Modifier.size(56.dp).background(Color(0xFF1877F2), CircleShape)
                    ) {
                        Icon(Icons.Default.Facebook, null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { uriHandler.openUri("https://instagram.com/reynelbusto") },
                        modifier = Modifier.size(56.dp).background(Color(0xFFE4405F), CircleShape)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                    }
                    IconButton(
                        onClick = { uriHandler.openUri("mailto:reynelbusto@gmail.com") },
                        modifier = Modifier.size(56.dp).background(Color(0xFFEA4335), CircleShape)
                    ) {
                        Icon(Icons.Default.Email, null, tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Desarrollado con ❤️ por Reynel Busto",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- Drawing Screen ---
@Composable
fun DrawingScreen(viewModel: YuliAppViewModel) {
    var paths by remember { mutableStateOf(listOf<Pair<Path, Color>>()) }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentColor by remember { mutableStateOf(com.example.ui.theme.MagicTertiary) }
    
    val colors = listOf(
        com.example.ui.theme.MagicTertiary,
        com.example.ui.theme.MagicSecondary,
        com.example.ui.theme.MagicSun,
        Color(0xFF4CAF50),
        com.example.ui.theme.ToddleOrange,
        com.example.ui.theme.MagicPrimary
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val path = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = path
                            try {
                                awaitRelease()
                                currentPath?.let { paths = paths + (it to currentColor) }
                                currentPath = null
                            } catch (e: Exception) {
                                currentPath = null
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                }
                            }
                        }
                    }
                }
        ) {
            paths.forEach { (path, color) ->
                drawPath(path = path, color = color, style = Stroke(width = 8.dp.toPx()))
            }
            currentPath?.let {
                drawPath(path = it, color = currentColor, style = Stroke(width = 8.dp.toPx()))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { viewModel.setScreen("home") },
                modifier = Modifier.background(Color.White, CircleShape).shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.Default.Home, null, tint = com.example.ui.theme.MagicSecondary)
            }
            IconButton(
                onClick = { paths = emptyList() },
                modifier = Modifier.background(Color.White, CircleShape).shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.Default.Delete, null, tint = com.example.ui.theme.MagicTertiary)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, CircleShape)
                        .border(
                            width = if (currentColor == color) 4.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { currentColor = color }
                )
            }
        }
    }
}

@Composable
fun LegalScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        androidx.compose.ui.res.stringResource(R.string.title_privacy),
        androidx.compose.ui.res.stringResource(R.string.title_terms),
        androidx.compose.ui.res.stringResource(R.string.title_credits)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(4.dp, com.example.ui.theme.MagicSecondary, RoundedCornerShape(24.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = com.example.ui.theme.MagicSecondary)
            }
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.title_legal),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.example.ui.theme.MagicSecondary
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = com.example.ui.theme.MagicSecondary,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = com.example.ui.theme.MagicSecondary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val contentText = when (selectedTab) {
                0 -> androidx.compose.ui.res.stringResource(R.string.privacy_policy_text)
                1 -> androidx.compose.ui.res.stringResource(R.string.terms_text)
                else -> androidx.compose.ui.res.stringResource(R.string.credits_text)
            }
            Text(
                text = contentText.trimIndent(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun AddAnimalDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🦄") }
    var selectedColor by remember { mutableStateOf("#ff4b4b") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Animal", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Emoji:", fontSize = 14.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("🦄", "🐉", "🐙", "🐯", "🐼", "🦊", "🐻", "🐝").forEach { e ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (emoji == e) Color.LightGray else Color.Transparent)
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 28.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name, emoji, selectedColor) }) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AddFamilyMemberDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var relation by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("👦") }
    var selectedColor by remember { mutableStateOf("#90CAF9") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Familiar", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Parentesco (ej. Hermano)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Emoji representativo:", fontSize = 14.sp)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("👦", "👧", "👶", "👱‍♀️", "👱‍♂️", "👩‍🦳", "👨‍🦳", "🐶").forEach { e ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (emoji == e) Color.LightGray else Color.Transparent)
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 28.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (relation.isNotBlank()) onConfirm(relation, emoji, selectedColor) }) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    val pages = listOf(
        TutorialPage("Formas 🎨", "Toca colores y aprende figuras divertidas.", "🎨", com.example.ui.theme.MagicSecondary),
        TutorialPage("La Granja 🐮", "Escucha animales reales y ¡graba tu propia voz!", "🐮", Color(0xFF4CAF50)),
        TutorialPage("Piano 🎹", "Sigue las estrellas brillantes para tocar canciones.", "🌟", com.example.ui.theme.MagicTertiary),
        TutorialPage("Dibujo 🖍️", "Estimulación táctil con trazos de colores mágicos.", "🖍️", Color(0xFF4CAF50)),
        TutorialPage("Familia 👩", "Crea tarjetas con fotos y voces de tus seres queridos.", "💖", com.example.ui.theme.ToddleOrange),
        TutorialPage("Modo Padres 🔒", "Mantén el candado para personalizar todo el juego.", "⚙️", Color.Gray)
    )
    val pagerState = rememberPagerState { pages.size }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Guía de YuliApp 🎈",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = com.example.ui.theme.MagicPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = page.color.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(page.emoji, fontSize = 56.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            page.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = page.color
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            page.description,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) com.example.ui.theme.MagicSecondary else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("¡ENTENDIDO!", fontWeight = FontWeight.Black, color = com.example.ui.theme.MagicSecondary)
                }
            }
        }
    }
}

data class TutorialPage(val title: String, val description: String, val emoji: String, val color: Color)

@Composable
fun RecordVoiceDialog(
    title: String,
    isRecording: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
    extraContent: @Composable () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) Color.Red.copy(alpha = 0.1f) else Color(0xFFF5F5F5))
                        .clickable {
                            if (isRecording) onStop() else onStart()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val scale by rememberInfiniteTransition().animateFloat(
                        initialValue = 1f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                        label = "pulse"
                    )
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(if (isRecording) scale else 1f),
                        tint = if (isRecording) Color.Red else Color(0xFF455A64)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRecording) "Grabando..." else "Toca para grabar",
                    color = if (isRecording) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onImport,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.AudioFile, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar MP3/WAV")
                }

                extraContent()

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Listo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
