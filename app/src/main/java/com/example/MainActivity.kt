package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalUriHandler
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AnimalEntity
import com.example.data.FamilyEntity
import com.example.ui.audio.YuliAudioSynth
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.YuliAppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri
import android.content.Intent
import android.provider.MediaStore

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
fun LoopingVideoBackground(videoPath: String, modifier: Modifier = Modifier) {
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
        update = { videoView ->
            try {
                videoView.setVideoPath(videoPath)
                videoView.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun VideoBackgroundContainer(videoPath: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        LoopingVideoBackground(videoPath = videoPath)
        // Semi-translucent overlay to ensure high readability and eye-safety
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.25f))
        )
    }
}

@Composable
fun AnimatedBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    // Rich, beautiful sensory color 1: Deep Navy Blue to Electric Navy Blue
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF07111E), // Dark Navy Blue
        targetValue = Color(0xFF0F2B48),  // Mid Navy Blue
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color1"
    )

    // Rich, beautiful sensory color 2: Vibrant Yellow-Green (Lime) to Electric Cyan Blue
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFFAEEA00), // Vivid Lime Green
        targetValue = Color(0xFF00E5FF),  // Electric Sky Blue / Cyan
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color2"
    )

    // Rich, beautiful sensory color 3: Bright Lemon Yellow to Warm Sun Yellow
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFFFFEA00), // Vivid Sunny Yellow
        targetValue = Color(0xFFFFD600),  // Bright Golden Yellow
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color3"
    )

    // Bubble animations for a lovely interactive floating feel
    val progress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble1"
    )

    val progress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble2"
    )

    val progress3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(21000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble3"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Draw dynamic, highly-visible sensory fluid gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(color1, color2, color3),
                start = Offset(0f, 0f),
                end = Offset(width, height)
            )
        )

        // Floating bubbles with increased opacity to stand out beautifully
        // Bubble 1
        val y1 = height - (height + 300f) * progress1
        val x1 = width * 0.25f + 80f * kotlin.math.sin(progress1 * 2f * kotlin.math.PI.toFloat())
        drawCircle(
            color = Color.White.copy(alpha = 0.42f),
            radius = 130f,
            center = Offset(x1, y1)
        )

        // Bubble 2
        val y2 = height - (height + 400f) * progress2
        val x2 = width * 0.75f - 100f * kotlin.math.sin(progress2 * 2f * kotlin.math.PI.toFloat())
        drawCircle(
            color = Color.White.copy(alpha = 0.38f),
            radius = 190f,
            center = Offset(x2, y2)
        )

        // Bubble 3
        val y3 = height - (height + 250f) * progress3
        val x3 = width * 0.5f + 60f * kotlin.math.cos(progress3 * 2f * kotlin.math.PI.toFloat())
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = 100f,
            center = Offset(x3, y3)
        )
    }
}

@Composable
fun YuliApp(viewModel: YuliAppViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isParentMode by viewModel.isParentMode.collectAsState()
    val backgroundVideoPath by viewModel.backgroundVideoPath.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // High-fidelity bottom navigation with giant poppable bubbles
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
                AnimatedBackground()
            }

            // Parent Mode Status Banner at the top
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
                        }
                    )
                }

                // Main screen container
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "home" -> HomeScreen(viewModel = viewModel)
                        "colors" -> ColorsScreen(viewModel = viewModel)
                        "animals" -> AnimalsScreen(viewModel = viewModel)
                        "piano" -> PianoScreen(viewModel = viewModel)
                        "family" -> FamilyScreen(viewModel = viewModel)
                        "options" -> OptionsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// --- TOP BANNER FOR PARENT CONTROLS ---
@Composable
fun ParentModeBanner(onLockAgain: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3E0))
            .padding(vertical = 10.dp, horizontal = 16.dp)
            .testTag("parent_mode_banner"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = Color(0xFFE65100),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "🛠️ MODO PADRES: Grabación y Ajustes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
        }
        Button(
            onClick = onLockAgain,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Bloquear",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cerrar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- HOME SCREEN ---
@Composable
fun HomeScreen(viewModel: YuliAppViewModel) {
    val isParentMode by viewModel.isParentMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "YuliApp 🎈",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFEA00), // Vivid Sunny Yellow
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¡Juega, toca y escucha!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White, // White for amazing contrast on dark/navy background
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = {
                    YuliAudioSynth.playPop()
                    viewModel.setScreen("options")
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF00E5FF), CircleShape) // Electric Cyan
                    .shadow(3.dp, CircleShape)
                    .testTag("home_options_button")
            ) {
                Text("🎁", fontSize = 24.sp)
            }
        }

        // Giant Grid with 4 category buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CategoryBubble(
                    title = "Colores",
                    emoji = "🎨",
                    color = Color(0xFF0D47A1), // Deep Navy Blue
                    testTag = "category_colors",
                    onClick = { viewModel.setScreen("colors") }
                )
            }
            item {
                CategoryBubble(
                    title = "Animales",
                    emoji = "🐮",
                    color = Color(0xFFAEEA00), // Vivid Lime / Yellow-Green
                    testTag = "category_animals",
                    onClick = { viewModel.setScreen("animals") }
                )
            }
            item {
                CategoryBubble(
                    title = "Piano",
                    emoji = "🎹",
                    color = Color(0xFF00E5FF), // Electric Cyan / Sky Blue
                    testTag = "category_piano",
                    onClick = { viewModel.setScreen("piano") }
                )
            }
            item {
                CategoryBubble(
                    title = "Familia",
                    emoji = "👩",
                    color = Color(0xFFFFEA00), // Vivid Sunny Yellow
                    testTag = "category_family",
                    onClick = { viewModel.setScreen("family") }
                )
            }
        }

        // Parent Unlock Section: Requires 3 seconds long press
        ParentLockSection(
            isParentMode = isParentMode,
            onUnlocked = {
                viewModel.toggleParentMode()
            }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(bottom = 16.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = color.copy(alpha = 0.25f),
                spotColor = color.copy(alpha = 0.5f)
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag(testTag)
            .clickable(
                onClick = {
                    YuliAudioSynth.playForwardNavigation()
                    onClick()
                }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Glass Reflection & Radial Color Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                color.copy(alpha = 0.12f)
                            ),
                            radius = 320f
                        )
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(color.copy(alpha = 0.18f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 42.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3436)
                )
            }

            // Glossy highlight reflection (bubble-float)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent),
                            radius = 180f
                        )
                    )
            )
        }
    }
}

@Composable
fun ParentLockSection(
    isParentMode: Boolean,
    onUnlocked: () -> Unit
) {
    var holdProgress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Handle smooth visual animation of holding down
    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            val duration = 2500f // 2.5 seconds hold required
            while (isHolding && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / duration).coerceAtMost(1f)
                if (holdProgress >= 1f) {
                    onUnlocked()
                    isHolding = false
                    holdProgress = 0f
                }
                delay(30)
            }
        } else {
            // Cool spring-like return
            while (holdProgress > 0f) {
                holdProgress = (holdProgress - 0.1f).coerceAtLeast(0f)
                delay(20)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .testTag("parent_lock_section")
    ) {
        Text(
            text = if (isParentMode) "🔓 Ajustes Activos" else "🔒 Mantén presionado para Ajustes de Padres",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isParentMode) Color(0xFFE65100) else Color(0xFF78909C),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Large Lock bubble button
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(6.dp, CircleShape)
                .background(
                    if (isParentMode) Color(0xFFE65100) else Color(0xFF90A4AE),
                    CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            try {
                                awaitRelease()
                            } finally {
                                isHolding = false
                            }
                        }
                    )
                }
                .testTag("parent_lock_button"),
            contentAlignment = Alignment.Center
        ) {
            // Holding animated progress circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (holdProgress > 0f) {
                    drawArc(
                        color = Color(0xFFFFD54F),
                        startAngle = -90f,
                        sweepAngle = holdProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
            }
            Icon(
                imageVector = if (isParentMode) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = "Parent Control Lock",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// --- CUSTOM BOTTOM BAR NAVIGATION ---
@Composable
fun YuliBottomBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        color = Color.White.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                emoji = "🏠",
                label = "Inicio",
                isActive = currentScreen == "home",
                testTag = "nav_home",
                onClick = { onNavigate("home") }
            )
            BottomNavItem(
                emoji = "🎨",
                label = "Colores",
                isActive = currentScreen == "colors",
                testTag = "nav_colors",
                onClick = { onNavigate("colors") }
            )
            BottomNavItem(
                emoji = "🐮",
                label = "Animales",
                isActive = currentScreen == "animals",
                testTag = "nav_animals",
                onClick = { onNavigate("animals") }
            )
            BottomNavItem(
                emoji = "🎹",
                label = "Piano",
                isActive = currentScreen == "piano",
                testTag = "nav_piano",
                onClick = { onNavigate("piano") }
            )
            BottomNavItem(
                emoji = "👩",
                label = "Familia",
                isActive = currentScreen == "family",
                testTag = "nav_family",
                onClick = { onNavigate("family") }
            )
            BottomNavItem(
                emoji = "💝",
                label = "Donar",
                isActive = currentScreen == "options",
                testTag = "nav_options",
                onClick = { onNavigate("options") }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    emoji: String,
    label: String,
    isActive: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1.2f else 1.0f)
    val bgColor = if (isActive) Color(0xFF0D47A1).copy(alpha = 0.12f) else Color.Transparent
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = Color(0xFF0D47A1).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 28.sp)
        if (isActive) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0D47A1)
            )
        }
    }
}

// --- COLORS GAME SCREEN ---
data class QuizQuestion(
    val targetShape: String,
    val targetColorName: String,
    val targetColor: Color,
    val options: List<Pair<String, String>> // Pair of Shape, ColorName
)

@Composable
fun SensoryShapeItem(shape: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = androidx.compose.ui.graphics.Path()
        val width = size.width
        val height = size.height
        when (shape) {
            "Círculo" -> {
                drawCircle(color = color, radius = size.minDimension * 0.42f)
            }
            "Cuadrado" -> {
                drawRoundRect(
                    color = color,
                    size = Size(width * 0.8f, height * 0.8f),
                    topLeft = Offset(width * 0.1f, height * 0.1f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                )
            }
            "Triángulo" -> {
                path.moveTo(width / 2f, height * 0.15f)
                path.lineTo(width * 0.85f, height * 0.85f)
                path.lineTo(width * 0.15f, height * 0.85f)
                path.close()
                drawPath(path = path, color = color)
            }
            "Estrella" -> {
                val centerX = width / 2f
                val centerY = height / 2f
                val r = Math.min(width, height) / 2f * 0.85f
                val innerR = r * 0.42f
                for (i in 0 until 10) {
                    val angle = i * Math.PI / 5 - Math.PI / 2
                    val currR = if (i % 2 == 0) r else innerR
                    val x = centerX + currR * Math.cos(angle).toFloat()
                    val y = centerY + currR * Math.sin(angle).toFloat()
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                drawPath(path = path, color = color)
            }
        }
    }
}

@Composable
fun ColorsScreen(viewModel: YuliAppViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val colorsList = listOf(
        Triple("ROJO", Color(0xFFFF4B4B), "❤️"),
        Triple("AMARILLO", Color(0xFFFFD54F), "💛"),
        Triple("AZUL", Color(0xFF2196F3), "💙"),
        Triple("VERDE", Color(0xFF4CAF50), "💚"),
        Triple("NARANJA", Color(0xFFFF9800), "🧡"),
        Triple("MORADO", Color(0xFF9C27B0), "💜")
    )

    val colorsMap = remember {
        mapOf(
            "Rojo" to Color(0xFFFF4B4B),
            "Amarillo" to Color(0xFFFFD54F),
            "Azul" to Color(0xFF2196F3),
            "Verde" to Color(0xFF4CAF50),
            "Naranja" to Color(0xFFFF9800),
            "Morado" to Color(0xFF9C27B0)
        )
    }

    var isQuizMode by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var feedbackMessage by remember { mutableStateOf("") }
    var showSuccessCelebration by remember { mutableStateOf(false) }

    fun generateNewQuestion() {
        val shapes = listOf("Círculo", "Cuadrado", "Triángulo", "Estrella")
        val colorNames = listOf("Rojo", "Amarillo", "Azul", "Verde", "Naranja", "Morado")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Simple big home escape button & quiz toggle
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
                    .testTag("colors_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ir al inicio",
                    tint = Color(0xFFFF4B4B),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Game Mode switch
            Button(
                onClick = {
                    YuliAudioSynth.playPop()
                    isQuizMode = !isQuizMode
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isQuizMode) Color(0xFFFF4B4B) else Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("colors_quiz_mode_toggle")
            ) {
                Text(
                    text = if (isQuizMode) "🧩 MODO LIBRE" else "🎮 MODO JUEGO",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isQuizMode) {
            Text(
                text = "🌈 ¡Toca un Color!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Giant colored bubble keys
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(colorsList) { item ->
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else 1.0f,
                        animationSpec = tween(100)
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
                            .aspectRatio(0.95f)
                            .scale(scale)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(36.dp),
                                ambientColor = item.second.copy(alpha = 0.5f),
                                spotColor = item.second.copy(alpha = 0.9f)
                            )
                            .clip(RoundedCornerShape(36.dp))
                            .background(item.second)
                            .clickable {
                                isPressed = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val freq = when (item.first) {
                                    "ROJO" -> 261.63     // C4
                                    "NARANJA" -> 293.66  // D4
                                    "AMARILLO" -> 329.63 // E4
                                    "VERDE" -> 392.00    // G4
                                    "AZUL" -> 440.00     // A4
                                    "MORADO" -> 523.25   // C5
                                    else -> 392.00
                                }
                                YuliAudioSynth.playMarimbaNote(freq, 350)
                            }
                            .testTag("color_bubble_${item.first.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.4f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = item.third, fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.first,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.first == "AMARILLO") Color(0xFF5D4037) else Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // INTERACTIVE QUIZ GAME SCREEN
            currentQuestion?.let { question ->
                Text(
                    text = "🤔 ¿Dónde está el...?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .border(
                            width = 2.dp,
                            color = question.targetColor.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${question.targetShape.uppercase()} ${question.targetColorName.uppercase()}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = question.targetColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = feedbackMessage,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of options
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    items(question.options) { option ->
                        val shapeName = option.first
                        val colorName = option.second
                        val shapeColor = colorsMap[colorName] ?: Color.Gray

                        val isTarget = shapeName == question.targetShape && colorName == question.targetColorName

                        var isOptionPressed by remember { mutableStateOf(false) }
                        val scaleOpt by animateFloatAsState(
                            targetValue = if (isOptionPressed) 0.88f else 1.0f,
                            animationSpec = tween(120)
                        )

                        LaunchedEffect(isOptionPressed) {
                            if (isOptionPressed) {
                                delay(120)
                                isOptionPressed = false
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.95f)
                                .scale(scaleOpt)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(28.dp),
                                    ambientColor = shapeColor.copy(alpha = 0.3f),
                                    spotColor = shapeColor.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White)
                                .clickable {
                                    isOptionPressed = true
                                    if (isTarget) {
                                        feedbackMessage = "🎉 ¡EXCELENTE! ¡LO LOGRASTE!"
                                        showSuccessCelebration = true
                                        // Play happy melody sweep
                                        YuliAudioSynth.playTone(392.00, 180, "sine", 523.25)
                                        // Advance to new question after short celebration
                                        coroutineScope.launch {
                                            delay(1500)
                                            generateNewQuestion()
                                        }
                                    } else {
                                        feedbackMessage = "¡Casi! Inténtalo otra vez 😊"
                                        // Play soft wobble sound
                                        YuliAudioSynth.playTone(220.0, 180, "sine", 160.0)
                                    }
                                }
                                .testTag("quiz_option_${shapeName.lowercase()}_${colorName.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Gloss highlight overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                                            radius = 150f
                                        )
                                    )
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize().padding(12.dp)
                            ) {
                                SensoryShapeItem(
                                    shape = shapeName,
                                    color = shapeColor,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .padding(4.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "$shapeName\n$colorName",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF37474F),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ANIMAL FARM GAME SCREEN (DEEP DIVE) ---
@Composable
fun AnimalsScreen(viewModel: YuliAppViewModel) {
    val animalsList by viewModel.animals.collectAsState()
    val isParentMode by viewModel.isParentMode.collectAsState()
    val recordingId by viewModel.recordingEntityId.collectAsState()
    val playingPath by viewModel.playingCustomSoundPath.collectAsState()
    val playingAudioId by viewModel.playingAudioId.collectAsState()
    val context = LocalContext.current
    var showAddAnimalDialog by remember { mutableStateOf(false) }

    // Request dynamic microphone permission
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permiso de micrófono necesario para grabar voces.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Exit/Home Button & Add Animal button
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
                    .testTag("animals_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ir al inicio",
                    tint = Color(0xFF81C784),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Parent Add Animal Button
            if (isParentMode) {
                Button(
                    onClick = {
                        YuliAudioSynth.playPop()
                        showAddAnimalDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("animals_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🐷 La Granja de Sonidos",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main animal collection scrollable list with giant cards
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(animalsList) { animal ->
                AnimalBubbleCard(
                    animal = animal,
                    isParentMode = isParentMode,
                    isRecordingThis = recordingId == "animal_${animal.id}",
                    isPlayingThis = (playingPath == animal.customSoundPath && playingPath != null) || (playingAudioId == animal.id),
                    onTap = { viewModel.playAnimalSound(animal) },
                    onRecordToggle = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            val entityId = "animal_${animal.id}"
                            if (recordingId == entityId) {
                                viewModel.stopRecordingAndSaveAnimal(animal)
                                Toast.makeText(context, "¡Sonido grabado con éxito!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.startRecording(entityId)
                            }
                        }
                    },
                    onDeleteCustom = {
                        viewModel.deleteAnimalVoice(animal)
                        Toast.makeText(context, "Sonido grabado eliminado.", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteAnimal = {
                        viewModel.deleteAnimal(animal.id)
                        Toast.makeText(context, "Animal eliminado de la granja.", Toast.LENGTH_SHORT).show()
                    },
                    onImportAudio = { uri ->
                        viewModel.saveImportedAudioForAnimal(animal, uri)
                    },
                    onToggleMode = { isCustom ->
                        viewModel.toggleAnimalCustomVoice(animal, isCustom)
                    }
                )
            }
        }
    }

    if (showAddAnimalDialog) {
        AddAnimalDialog(
            onDismiss = { showAddAnimalDialog = false },
            onSave = { name, emoji, colorHex ->
                viewModel.addAnimal(name, emoji, colorHex)
                showAddAnimalDialog = false
            }
        )
    }
}

@Composable
fun AnimalBubbleCard(
    animal: AnimalEntity,
    isParentMode: Boolean,
    isRecordingThis: Boolean,
    isPlayingThis: Boolean,
    onTap: () -> Unit,
    onRecordToggle: () -> Unit,
    onDeleteCustom: () -> Unit,
    onDeleteAnimal: () -> Unit,
    onImportAudio: (android.net.Uri) -> Unit,
    onToggleMode: (Boolean) -> Unit
) {
    val parsedColor = remember(animal.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(animal.colorHex))
        } catch (e: Exception) {
            Color(0xFF81C784)
        }
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImportAudio(uri)
            Toast.makeText(context, "¡Audio subido con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = parsedColor.copy(alpha = 0.25f),
                spotColor = parsedColor.copy(alpha = 0.5f)
            )
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("animal_card_${animal.id}"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Giant tactile tap circle for animal sound with a glossy bubble dome
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.White, parsedColor.copy(alpha = 0.18f))
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTap()
                    }
                    .testTag("animal_tap_zone_${animal.id}"),
                contentAlignment = Alignment.Center
            ) {
                // Play feedback visual halo
                if (isPlayingThis) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(parsedColor.copy(alpha = 0.35f))
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = animal.emoji, fontSize = 80.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = animal.name.uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2D3436)
                    )
                }

                // Glossy reflection overlay (bubble-float)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                                radius = 120f
                            )
                        )
                )
            }

            // Custom sound status for baby mode
            if (!isParentMode && animal.customSoundPath != null && animal.isCustomSoundEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "🗣️ ¡Con la voz de Mamá/Papá!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            // PARENT CUSTOMIZATION CONTROLS
            if (isParentMode) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FBE7), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚙️ AJUSTE DE SONIDOS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF558B2F)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic recorder button
                        Button(
                            onClick = onRecordToggle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecordingThis) Color.Red else Color(0xFF558B2F)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("record_mic_${animal.id}")
                        ) {
                            Icon(
                                imageVector = if (isRecordingThis) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Micrófono"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRecordingThis) "PARAR" else "GRABAR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Upload Audio button (MP3/WAV)
                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("upload_audio_${animal.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Subir audio"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SUBIR FILE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Play Test button (enabled if custom sound exists)
                        if (animal.customSoundPath != null) {
                            IconButton(
                                onClick = onTap,
                                modifier = Modifier
                                    .background(Color(0xFFDCEDC8), CircleShape)
                                    .size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Escuchar grabación",
                                    tint = Color(0xFF558B2F)
                                )
                            }

                            IconButton(
                                onClick = onDeleteCustom,
                                modifier = Modifier
                                    .background(Color(0xFFFFCDD2), CircleShape)
                                    .size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar grabación",
                                    tint = Color.Red
                                )
                            }
                        }

                        // Delete custom added animal completely
                        if (animal.id.startsWith("custom_")) {
                            IconButton(
                                onClick = onDeleteAnimal,
                                modifier = Modifier
                                    .background(Color(0xFFECEFF1), CircleShape)
                                    .size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar animal",
                                    tint = Color(0xFF37474F)
                                )
                            }
                        }
                    }

                    // Toggle custom sound active
                    if (animal.customSoundPath != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Usar sonido grabado por padres",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F)
                            )
                            Switch(
                                checked = animal.isCustomSoundEnabled,
                                onCheckedChange = { onToggleMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF558B2F),
                                    checkedTrackColor = Color(0xFFDCEDC8)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- CUSTOM DIALOG TO ADD ANIMAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnimalDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🐰") }
    var selectedColorHex by remember { mutableStateOf("#E8F5E9") }

    val emojis = listOf("🐰", "🐴", "🐔", "🦊", "🐻", "🐼", "🐨", "🐯", "🐵", "🐹", "🐝")
    val colors = listOf("#ff4b4b", "#90CAF9", "#CE93D8", "#80CBC4", "#FFD54F", "#FFB74D", "#E8F5E9")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_animal_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Añadir Animal a la Granja ➕",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Animal (ej: Conejo)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_animal_name")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Elige un Emoji:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.take(6).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedEmoji == emoji) Color(0xFFC8E6C9) else Color.Transparent
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.drop(6).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedEmoji == emoji) Color(0xFFC8E6C9) else Color.Transparent
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Elige un Color de fondo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { hex ->
                        val parsed = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .clickable { selectedColorHex = hex }
                                .testTag("animal_color_select_$hex"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorHex == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name, selectedEmoji, selectedColorHex)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_animal_button")
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- MUSIC PIANO SCREEN ---
@Composable
fun PianoScreen(viewModel: YuliAppViewModel) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val whiteKeys = listOf(
        Pair("C", 261.63),
        Pair("D", 293.66),
        Pair("E", 329.63),
        Pair("F", 349.23),
        Pair("G", 392.00),
        Pair("A", 440.00),
        Pair("B", 493.88),
        Pair("C+", 523.25)
    )

    val blackKeys = listOf(
        Triple("C#", 277.18, 0.5f),
        Triple("D#", 311.13, 1.5f),
        Triple("F#", 369.99, 3.5f),
        Triple("G#", 415.30, 4.5f),
        Triple("A#", 466.16, 5.5f)
    )

    val keyColors = listOf(
        Color(0xFFFFEA00), // C -> Vivid Sunny Yellow
        Color(0xFFCCFF00), // D -> Vivid Chartreuse / Lime Yellow
        Color(0xFFAEEA00), // E -> Vivid Lime Green
        Color(0xFF00BFA5), // F -> Vivid Teal / Mint
        Color(0xFF00E5FF), // G -> Electric Cyan / Sky Blue
        Color(0xFF2979FF), // A -> Vivid Cobalt Blue
        Color(0xFF1565C0), // B -> Bright Royal Blue
        Color(0xFF0D47A1)  // C+ -> Vivid Deep Navy Blue
    )

    val allKeysMap = remember {
        mapOf(
            "C" to 261.63,
            "D" to 293.66,
            "E" to 329.63,
            "F" to 349.23,
            "G" to 392.00,
            "A" to 440.00,
            "B" to 493.88,
            "C+" to 523.25,
            "C#" to 277.18,
            "D#" to 311.13,
            "F#" to 369.99,
            "G#" to 415.30,
            "A#" to 466.16
        )
    }

    // Kid songs list
    val songsList = listOf(
        Pair("Ninguno", emptyList<String>()),
        Pair("Estrellita ⭐", listOf("C", "C", "G", "G", "A", "A", "G", "F", "F", "E", "E", "D", "D", "C", "G", "G", "F", "F", "E", "E", "D", "G", "G", "F", "F", "E", "E", "D", "C", "C", "G", "G", "A", "A", "G", "F", "F", "E", "E", "D", "D", "C")),
        Pair("Fray Santiago 🔔", listOf("C", "D", "E", "C", "C", "D", "E", "C", "E", "F", "G", "E", "F", "G", "G", "A", "G", "F", "E", "C", "G", "A", "G", "F", "E", "C", "C", "G", "C", "C", "G", "C")),
        Pair("Un Elefante 🐘", listOf("C", "C", "C", "D", "E", "E", "D", "C", "D", "E", "F", "F", "E", "D", "C")),
        Pair("Baby Shark 🦈", listOf("D", "E", "G", "G", "G", "G", "G", "G", "G", "D", "E", "G", "G", "G", "G", "G", "G", "G", "D", "E", "G", "G", "G", "G", "G", "G", "G", "G", "G", "F")),
        Pair("La Rana 🐸", listOf("C", "E", "C", "E", "G", "A", "G", "F", "E", "D", "C", "D", "E", "F", "D", "E", "F", "G", "E", "F", "G", "A", "G", "F", "E", "D", "C")),
        Pair("Arroz con Leche 🍚", listOf("C", "D", "E", "C", "E", "G", "A", "G", "F", "E", "C", "D", "E", "E", "D", "D", "C")),
        Pair("Los Pollitos 🐣", listOf("C", "D", "E", "F", "G", "G", "A", "C+", "A", "G", "F", "F", "F", "F", "E", "E", "D", "D", "D", "D", "C")),
        Pair("Cascabel 🔔", listOf("E", "E", "E", "E", "E", "E", "E", "G", "C", "D", "E", "F", "F", "F", "F", "F", "E", "E", "E", "G", "G", "F", "D", "C"))
    )

    var activeSongName by remember { mutableStateOf("Ninguno") }
    var currentNoteIndex by remember { mutableStateOf(0) }
    val activeSongNotes = remember(activeSongName) {
        songsList.firstOrNull { it.first == activeSongName }?.second ?: emptyList()
    }

    // Infant UX Toggles & Selections
    var showBlackKeys by remember { mutableStateOf(true) }
    var selectedInstrument by remember { mutableStateOf("chimes") } // "chimes" (Toy Piano) or "wood" (Marimba)

    // Touch Engine Tracking States
    var parentCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val keyCoordinatesMap = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    val currentlyPressedKeys = remember { mutableStateListOf<String>() }
    var lastPressedKeys by remember { mutableStateOf(setOf<String>()) }

    // Visual Particle System
    val floatingBubbles = remember { mutableStateListOf<InstrumentBubble>() }
    var nextBubbleId by remember { mutableLongStateOf(0L) }

    fun findKeyAtPosition(
        position: Offset,
        coordinatesMap: Map<String, LayoutCoordinates>,
        parentCoords: LayoutCoordinates,
        showBlack: Boolean
    ): String? {
        if (showBlack) {
            for (bKey in blackKeys) {
                val coords = coordinatesMap[bKey.first]
                if (coords != null && coords.isAttached && parentCoords.isAttached) {
                    val rect = parentCoords.localBoundingBoxOf(coords, clipBounds = false)
                    if (rect.contains(position)) {
                        return bKey.first
                    }
                }
            }
        }
        for (wKey in whiteKeys) {
            val coords = coordinatesMap[wKey.first]
            if (coords != null && coords.isAttached && parentCoords.isAttached) {
                val rect = parentCoords.localBoundingBoxOf(coords, clipBounds = false)
                if (rect.contains(position)) {
                    return wKey.first
                }
            }
        }
        return null
    }

    fun spawnBubble(keyName: String, coords: LayoutCoordinates?, parentCoords: LayoutCoordinates) {
        if (coords == null || !coords.isAttached || !parentCoords.isAttached) return
        val rect = parentCoords.localBoundingBoxOf(coords, clipBounds = false)
        val centerX = rect.center.x
        val centerY = rect.top

        val colorIdx = whiteKeys.indexOfFirst { it.first == keyName.replace("#", "") }.coerceIn(0, 7)
        val color = keyColors[colorIdx]

        val emojis = listOf("🎈", "🎵", "🎶", "🌟", "⭐", "✨", "❤️", "🌈", "🍭", "🎨", "🐣", "🐸", "🐱", "🐶")
        val randomEmoji = emojis.random()

        val bubbleId = nextBubbleId++
        val newBubble = InstrumentBubble(
            id = bubbleId,
            text = randomEmoji,
            color = color,
            initialX = centerX,
            initialY = centerY,
            randomOffsetX = (Math.random() * 120 - 60).toFloat()
        )
        floatingBubbles.add(newBubble)

        coroutineScope.launch {
            delay(1500)
            floatingBubbles.removeAll { it.id == bubbleId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Compact Top bar: Escape Home Button & Infant UX Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Escapar
            IconButton(
                onClick = {
                    YuliAudioSynth.playBackNavigation()
                    viewModel.setScreen("home")
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
                    .shadow(2.dp, CircleShape)
                    .testTag("piano_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ir al inicio",
                    tint = Color(0xFF4FC3F7),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Black Keys Toggle Chip
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(12.dp))
                    .clickable {
                        YuliAudioSynth.playPop()
                        showBlackKeys = !showBlackKeys
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(if (showBlackKeys) Color(0xFF1E293B) else Color(0xFFB0BEC5), CircleShape)
                )
                Text(
                    text = "Teclas Negras: " + if (showBlackKeys) "SÍ" else "NO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF37474F)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Instrument Picker Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFCFD8DC), RoundedCornerShape(12.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    Pair("chimes", "🎵 Chimes"),
                    Pair("wood", "🪵 Marimba")
                ).forEach { (instId, label) ->
                    val isSelected = selectedInstrument == instId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF4FC3F7) else Color.Transparent)
                            .clickable {
                                YuliAudioSynth.playPop()
                                selectedInstrument = instId
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else Color(0xFF546E7A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Song Selector pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            songsList.forEach { songItem ->
                val isSelected = songItem.first == activeSongName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF81C784) else Color.White)
                        .clickable {
                            YuliAudioSynth.playPop()
                            activeSongName = songItem.first
                            currentNoteIndex = 0
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .shadow(if (isSelected) 0.dp else 1.dp, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = songItem.first
                            .replace(" ⭐", "")
                            .replace(" 🔔", "")
                            .replace(" 🐘", "")
                            .replace(" 🦈", "")
                            .replace(" 🐸", "")
                            .replace(" 🍚", "")
                            .replace(" 🐣", ""),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else Color(0xFF37474F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Song note feedback guide bar (Compact and elegant)
        if (activeSongNotes.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1).copy(alpha = 0.8f))
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sigue las notas para tocar: ${activeSongName.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val startIdx = Math.max(0, currentNoteIndex - 2)
                        val endIdx = Math.min(activeSongNotes.size, startIdx + 6)

                        for (i in startIdx until endIdx) {
                            val note = activeSongNotes[i]
                            val isCurrent = i == currentNoteIndex
                            val noteColorIdx = whiteKeys.indexOfFirst { it.first == note }.coerceIn(0, 7)
                            val noteColor = keyColors[noteColorIdx]

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) noteColor else noteColor.copy(alpha = 0.35f))
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 0.dp,
                                        color = if (isCurrent) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = note,
                                    color = if (isCurrent) Color.White else Color.Black.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Keyboard container with Custom Multitouch Drag/Tap handling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                .padding(6.dp)
                .onGloballyPositioned { coords ->
                    parentCoordinates = coords
                }
                .pointerInput(showBlackKeys, selectedInstrument, activeSongNotes.size, currentNoteIndex) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val parentCoords = parentCoordinates ?: continue

                            // Track all fingers currently pressed down on the screen
                            val activeChanges = event.changes.filter { it.pressed }
                            val activeKeys = mutableSetOf<String>()

                            for (change in activeChanges) {
                                val pos = change.position
                                val keyName = findKeyAtPosition(pos, keyCoordinatesMap, parentCoords, showBlackKeys)
                                if (keyName != null) {
                                    activeKeys.add(keyName)
                                }
                            }

                            // Trigger sounds and visual animations for newly pressed keys
                            for (key in activeKeys) {
                                if (key !in lastPressedKeys) {
                                    val freq = allKeysMap[key] ?: 261.63
                                    val isBlack = key.contains("#")

                                    if (selectedInstrument == "wood") {
                                        YuliAudioSynth.playMarimbaNote(freq, 350)
                                    } else {
                                        YuliAudioSynth.playNote(freq, isBlack)
                                    }

                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    // Spawn colorful bubble rising up
                                    spawnBubble(key, keyCoordinatesMap[key], parentCoords)

                                    // Song evaluation
                                    if (activeSongNotes.isNotEmpty() && currentNoteIndex < activeSongNotes.size) {
                                        if (key == activeSongNotes[currentNoteIndex]) {
                                            currentNoteIndex++
                                            if (currentNoteIndex >= activeSongNotes.size) {
                                                // Celebration fanfare!
                                                coroutineScope.launch {
                                                    YuliAudioSynth.playTone(523.25, 100, "sine", 659.25)
                                                    delay(120)
                                                    YuliAudioSynth.playTone(659.25, 100, "sine", 783.99)
                                                    delay(120)
                                                    YuliAudioSynth.playTone(783.99, 400, "sine", 1046.50)
                                                }
                                                currentNoteIndex = 0
                                            }
                                        }
                                    }
                                }
                            }

                            currentlyPressedKeys.clear()
                            currentlyPressedKeys.addAll(activeKeys)
                            lastPressedKeys = activeKeys
                        }
                    }
                }
                .testTag("piano_keyboard_container")
        ) {
            val keySpacing = 4.dp

            // 1. WHITE KEYS (Row)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(keySpacing)
            ) {
                whiteKeys.forEachIndexed { index, key ->
                    val isPressed = key.first in currentlyPressedKeys
                    val isTargetKey = activeSongNotes.isNotEmpty() &&
                            currentNoteIndex < activeSongNotes.size &&
                            key.first == activeSongNotes[currentNoteIndex]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 10.dp, topEnd = 10.dp))
                            .background(
                                if (isPressed) keyColors[index].copy(alpha = 0.75f) else keyColors[index]
                            )
                            .border(
                                width = if (isTargetKey) 3.dp else 0.dp,
                                color = if (isTargetKey) Color.Yellow else Color.Transparent,
                                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 10.dp, topEnd = 10.dp)
                            )
                            .onGloballyPositioned { coords ->
                                keyCoordinatesMap[key.first] = coords
                            }
                            .graphicsLayer(
                                scaleY = if (isPressed) 0.96f else 1.0f,
                                scaleX = if (isPressed) 0.97f else 1.0f
                            )
                            .testTag("piano_key_${key.first.lowercase()}"),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Gloss highlight on keys
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.15f)
                                        )
                                    )
                                )
                        )

                        // Highlight Target Note for Baby
                        if (isTargetKey) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp)
                                    .background(Color.Yellow, CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Text(
                            text = key.first,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }

            // 2. BLACK KEYS (Overlay positioned absolutely)
            if (showBlackKeys) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val containerWidth = maxWidth
                    val whiteKeyWidth = (containerWidth - (keySpacing * 7)) / 8f
                    val blackKeyWidth = whiteKeyWidth * 0.6f

                    blackKeys.forEach { bKey ->
                        val isPressed = bKey.first in currentlyPressedKeys
                        val leftWhiteKeyIndex = (bKey.third - 0.5f).toInt()
                        val xOffset = (whiteKeyWidth + keySpacing) * leftWhiteKeyIndex + whiteKeyWidth + (keySpacing * 0.5f) - (blackKeyWidth * 0.5f)

                        Box(
                            modifier = Modifier
                                .offset(x = xOffset)
                                .width(blackKeyWidth)
                                .fillMaxHeight(0.55f)
                                .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp, topStart = 6.dp, topEnd = 6.dp))
                                .background(if (isPressed) Color(0xFF607D8B) else Color(0xFF0F172A))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp, topStart = 6.dp, topEnd = 6.dp))
                                .onGloballyPositioned { coords ->
                                    keyCoordinatesMap[bKey.first] = coords
                                }
                                .graphicsLayer(
                                    scaleY = if (isPressed) 0.94f else 1.0f
                                )
                                .testTag("piano_black_key_${bKey.first.replace("#", "s").lowercase()}"),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Gloss highlight for black keys
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.15f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.25f)
                                            )
                                        )
                                    )
                            )

                            Text(
                                text = bKey.first,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            // 3. FLOATING BUBBLE PARTICLES OVERLAY
            floatingBubbles.forEach { bubble ->
                BubbleAnimationItem(bubble = bubble, onDelete = { floatingBubbles.remove(bubble) })
            }
        }
    }
}

// Helper Data Class for Interactive Particles
data class InstrumentBubble(
    val id: Long,
    val text: String,
    val color: Color,
    val initialX: Float,
    val initialY: Float,
    val randomOffsetX: Float
)

@Composable
fun BubbleAnimationItem(bubble: InstrumentBubble, onDelete: () -> Unit) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearOutSlowInEasing)
        )
        onDelete()
    }

    val progress = animProgress.value
    val yOffset = progress * -300f // Floats 300px upwards
    val xOffset = progress * bubble.randomOffsetX // Drifts sideways randomly
    val alpha = 1f - progress // Smooth fade-out
    val scale = 0.6f + progress * 0.9f // Grow and drift

    Box(
        modifier = Modifier
            .offset(
                x = with(LocalDensity.current) { (bubble.initialX + xOffset).toDp() },
                y = with(LocalDensity.current) { (bubble.initialY + yOffset).toDp() }
            )
            .graphicsLayer(
                alpha = alpha,
                scaleX = scale,
                scaleY = scale
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(bubble.color.copy(alpha = 0.28f), CircleShape)
                .border(1.dp, bubble.color.copy(alpha = 0.45f), CircleShape)
        )
        Text(
            text = bubble.text,
            fontSize = 20.sp
        )
    }
}

// --- FAMILY PHOTO CARDS SCREEN ---
@Composable
fun FamilyScreen(viewModel: YuliAppViewModel) {
    val familyList by viewModel.family.collectAsState()
    val isParentMode by viewModel.isParentMode.collectAsState()
    val recordingId by viewModel.recordingEntityId.collectAsState()
    val playingPath by viewModel.playingCustomSoundPath.collectAsState()
    val playingAudioId by viewModel.playingAudioId.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    // Permissions check
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
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
                    .testTag("family_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ir al inicio",
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Parent Add Member button
            if (isParentMode) {
                Button(
                    onClick = {
                        YuliAudioSynth.playPop()
                        showAddDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("family_add_member_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "👩‍❤️‍👨 Mi Familia",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(familyList) { member ->
                FamilyPolaroidCard(
                    member = member,
                    isParentMode = isParentMode,
                    isRecordingThis = recordingId == "family_${member.id}",
                    isPlayingThis = (playingPath == member.customSoundPath && playingPath != null) || (playingAudioId == "family_${member.id}"),
                    onTap = { viewModel.playFamilySound(member) },
                    onRecordToggle = {
                        if (!hasMicPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            val entityId = "family_${member.id}"
                            if (recordingId == entityId) {
                                viewModel.stopRecordingAndSaveFamily(member)
                                Toast.makeText(context, "¡Sonido familiar guardado!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.startRecording(entityId)
                            }
                        }
                    },
                    onDeleteCustom = {
                        viewModel.deleteFamilyVoice(member)
                        Toast.makeText(context, "Sonido de familiar eliminado.", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteMember = {
                        viewModel.deleteFamilyMember(member.id)
                        Toast.makeText(context, "Familiar eliminado de la lista.", Toast.LENGTH_SHORT).show()
                    },
                    onImportAudio = { uri ->
                        viewModel.saveImportedAudioForFamily(member, uri)
                    },
                    onToggleMode = { isCustom ->
                        viewModel.toggleFamilyCustomVoice(member, isCustom)
                    },
                    onUploadPhoto = { uri ->
                        viewModel.savePhotoForFamily(member, uri)
                    },
                    onDeletePhoto = {
                        viewModel.deleteFamilyPhoto(member)
                        Toast.makeText(context, "Foto del familiar eliminada.", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Custom Add Family Member Dialog
    if (showAddDialog) {
        AddFamilyMemberDialog(
            onDismiss = { showAddDialog = false },
            onSave = { relation, emoji, colorHex ->
                viewModel.addFamilyMember(relation, emoji, colorHex)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun FamilyPolaroidCard(
    member: FamilyEntity,
    isParentMode: Boolean,
    isRecordingThis: Boolean,
    isPlayingThis: Boolean,
    onTap: () -> Unit,
    onRecordToggle: () -> Unit,
    onDeleteCustom: () -> Unit,
    onDeleteMember: () -> Unit,
    onImportAudio: (android.net.Uri) -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onUploadPhoto: (android.net.Uri) -> Unit,
    onDeletePhoto: () -> Unit
) {
    val parsedColor = remember(member.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(member.colorHex))
        } catch (e: Exception) {
            Color(0xFFFFD54F)
        }
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImportAudio(uri)
            Toast.makeText(context, "¡Audio familiar subido!", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onUploadPhoto(uri)
        }
    }

    // Classic soft Polaroid Style card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .testTag("family_card_${member.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Photo" frame space inside Polaroid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(parsedColor.copy(alpha = 0.15f))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTap()
                    }
                    .testTag("family_tap_zone_${member.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (member.customPhotoPath != null) {
                    AsyncImage(
                        model = member.customPhotoPath,
                        contentDescription = "Foto de ${member.relation}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = member.emoji,
                        fontSize = 64.sp
                    )
                }

                if (isPlayingThis) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(parsedColor.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Handwritten-like relation label
            Text(
                text = member.relation,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3E2723),
                textAlign = TextAlign.Center
            )

            // Audio notice
            if (!isParentMode && member.customSoundPath != null && member.isCustomSoundEnabled) {
                Text(
                    text = "🔊 ¡Tiene voz!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF558B2F),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // PARENT CUSTOMIZATION CONTROLS IN POLAROID
            if (isParentMode) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFDE7), RoundedCornerShape(10.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Button
                        IconButton(
                            onClick = onRecordToggle,
                            modifier = Modifier
                                .background(
                                    if (isRecordingThis) Color.Red else Color(0xFFFBC02D),
                                    CircleShape
                                )
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (isRecordingThis) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Grabar voz",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Upload Audio Button
                        IconButton(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            modifier = Modifier
                                .background(Color(0xFF00796B), CircleShape)
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Subir audio",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Play Test Button
                        if (member.customSoundPath != null) {
                            IconButton(
                                onClick = onTap,
                                modifier = Modifier
                                    .background(Color(0xFFFFF9C4), CircleShape)
                                    .size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Probar sonido",
                                    tint = Color(0xFFFBC02D),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onDeleteCustom,
                                modifier = Modifier
                                    .background(Color(0xFFFFCDD2), CircleShape)
                                    .size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar sonido",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Delete Member Card completely
                        IconButton(
                            onClick = onDeleteMember,
                            modifier = Modifier
                                .background(Color(0xFFECEFF1), CircleShape)
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar familiar",
                                tint = Color(0xFF546E7A),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Enable recorded voice toggle
                    if (member.customSoundPath != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Usar voz", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = member.isCustomSoundEnabled,
                                onCheckedChange = { onToggleMode(it) },
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFBC02D)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Photo upload button
                        IconButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .background(Color(0xFF4CAF50), CircleShape)
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Subir foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete photo button
                        if (member.customPhotoPath != null) {
                            IconButton(
                                onClick = onDeletePhoto,
                                modifier = Modifier
                                    .background(Color(0xFFEF5350), CircleShape)
                                    .size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (member.customPhotoPath != null) "Cambiar/Borrar Foto 📸" else "Subir Foto 📸",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3E2723),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

// --- CUSTOM DIALOG TO ADD FAMILY MEMBER ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var relation by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("👨‍👩- ") }
    var selectedColorHex by remember { mutableStateOf("#ff4b4b") }

    val emojis = listOf("👩", "👨", "👵", "👴", "👧", "👦", "👶", "🐶", "🐱", "🦁", "🐻")
    val colors = listOf("#ff4b4b", "#90CAF9", "#CE93D8", "#80CBC4", "#FFD54F", "#FFB74D", "#F48FB1")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_family_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Añadir Familiar ➕",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name/Relation input
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("¿Quién es? (ej: Tío, Hermano)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_relation_name")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Emoji list selector
                Text(
                    text = "Elige un Avatar/Emoji:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.take(6).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedEmoji == emoji) Color(0xFFFFF9C4) else Color.Transparent
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.drop(6).forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedEmoji == emoji) Color(0xFFFFF9C4) else Color.Transparent
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Color selector
                Text(
                    text = "Elige un Color de fondo:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { hex ->
                        val parsed = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsed)
                                .clickable { selectedColorHex = hex }
                                .testTag("color_select_$hex"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColorHex == hex) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (relation.isNotBlank() && selectedEmoji != "👨‍👩- ") {
                                onSave(relation, selectedEmoji, selectedColorHex)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_member_button")
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- OPTIONS & DONATIONS SCREEN ---
@Composable
fun OptionsScreen(viewModel: YuliAppViewModel) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val backgroundVideoPath by viewModel.backgroundVideoPath.collectAsState()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.saveBackgroundVideo(uri)
            Toast.makeText(context, "¡Fondo de video configurado con éxito!", Toast.LENGTH_SHORT).show()
        }
    }

    val videoRecordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                viewModel.saveBackgroundVideo(uri)
                Toast.makeText(context, "¡Video vertical grabado y configurado con éxito!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val recordVideoIntent = remember {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15) // Limit to 15 seconds
            putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)    // High quality
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
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
                    .testTag("options_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ir al inicio",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "💝 Opciones y Donaciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "¡Muchas gracias por jugar con YuliApp! Si te gusta esta aplicación y deseas apoyar al desarrollador para que siga creando experiencias educativas gratuitas para los niños, puedes donar o ponerte en contacto mediante los siguientes canales:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card for custom background video
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp))
                .testTag("custom_video_background_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎬 Fondo de Video Personalizado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A148C),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Personaliza YuliApp grabando un video vertical o subiendo uno desde tu dispositivo para que se reproduzca en bucle en el fondo de toda la aplicación.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF5D4037),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (backgroundVideoPath != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Text(text = "🟢 Fondo de video activo", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            YuliAudioSynth.playPop()
                            viewModel.clearBackgroundVideo()
                            Toast.makeText(context, "Fondo de video desactivado", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCDD2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("options_disable_video_button")
                    ) {
                        Text(text = "❌ Desactivar fondo de video", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            YuliAudioSynth.playPop()
                            try {
                                videoPickerLauncher.launch("video/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir la galería de videos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1BEE7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("options_upload_video_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📁 Subir", color = Color(0xFF4A148C), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "video", color = Color(0xFF4A148C), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            YuliAudioSynth.playPop()
                            try {
                                videoRecordLauncher.launch(recordVideoIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo abrir la cámara de video", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3E5FC)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("options_record_video_button")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📹 Grabar", color = Color(0xFF0277BD), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "video", color = Color(0xFF0277BD), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar/Initial Bubble
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE1BEE7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RB",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4A148C)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Reynel Busto",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723)
                )

                Text(
                    text = "Creador de YuliApp",
                    fontSize = 14.sp,
                    color = Color(0xFF795548),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Social Links
                SocialLinkItem(
                    emoji = "👤",
                    network = "Facebook",
                    value = "Reynel Busto",
                    color = Color(0xFF1877F2),
                    onClick = {
                        try {
                            uriHandler.openUri("https://www.facebook.com/reynel.busto")
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SocialLinkItem(
                    emoji = "💬",
                    network = "WhatsApp",
                    value = "+53 53466832",
                    color = Color(0xFF25D366),
                    onClick = {
                        try {
                            uriHandler.openUri("https://wa.me/5353466832")
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SocialLinkItem(
                    emoji = "📸",
                    network = "Instagram",
                    value = "@reynelbusto",
                    color = Color(0xFFE1306C),
                    onClick = {
                        try {
                            uriHandler.openUri("https://instagram.com/reynelbusto")
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SocialLinkItem(
                    emoji = "✉️",
                    network = "Correo",
                    value = "reynelbusto@gmail.com",
                    color = Color(0xFFDB4437),
                    onClick = {
                        try {
                            uriHandler.openUri("mailto:reynelbusto@gmail.com")
                        } catch (e: Exception) {
                            // Copy to clipboard fallback
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("reynelbusto_email", "reynelbusto@gmail.com")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Correo copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SocialLinkItem(
    emoji: String,
    network: String,
    value: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = network,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3E2723)
                    )
                }
            }
            Text(
                text = "👉",
                fontSize = 16.sp
            )
        }
    }
}
