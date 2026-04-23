package com.bluefirestudios.memeboard

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bluefirestudios.memeboard.ui.theme.MemeBoardTheme
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.ceil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemeBoardTheme {
                SoundboardScreen()
            }
        }
    }
}

data class SoundItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val resId: Int? = null,
    val filePath: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundboardScreen() {
    val context = LocalContext.current
    val soundsFile = remember(context) { File(context.filesDir, "sounds_list.txt") }

    var sounds by remember {
        mutableStateOf(listOf<SoundItem>())
    }

    var isEditMode by remember { mutableStateOf(false) }
    val isPlaying = remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var newSoundName by remember { mutableStateOf("") }

    // Load sounds
    LaunchedEffect(soundsFile) {
        if (soundsFile.exists()) {
            val lines = soundsFile.readLines()
            sounds = lines.mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 4) {
                    SoundItem(
                        id = parts[0],
                        name = parts[1],
                        resId = parts[2].toIntOrNull(),
                        filePath = parts[3].takeIf { it != "null" }
                    )
                } else null
            }
        } else {
            // Initial defaults
            sounds = listOf(
                SoundItem(name = "Dexter", resId = R.raw.dexter),
                SoundItem(name = "Bruh", resId = R.raw.bruh),
                SoundItem(name = "Bazooka", resId = R.raw.rip_my_granny),
                SoundItem(name = "Dial Up", resId = R.raw.dial_up),
                SoundItem(name = "Galaxy Meme", resId = R.raw.galaxy_meme),
                SoundItem(name = "Gay Echo", resId = R.raw.gay_echo),
                SoundItem(name = "Gop Gop Gop", resId = R.raw.gopgopgop),
                SoundItem(name = "Lego Breaking", resId = R.raw.lego_breaking),
                SoundItem(name = "FAHHHHH", resId = R.raw.fah),
                SoundItem(name = "Max Verstappen", resId = R.raw.max_verstrappen),
                SoundItem(name = "PLZ SPEED I NEED THISS", resId = R.raw.my_mom_is_kinda_homeless),
                SoundItem(name = "SIXX SEVENN", resId = R.raw.six_seven),
                SoundItem(name = "Vine Boom", resId = R.raw.vine_boom),
                SoundItem(name = "Oh My God", resId = R.raw.oh_my_god),
                SoundItem(name = "500 Cigarettes", resId = R.raw.fivehundred_cigarettes),
                SoundItem(name = "GET OUT", resId = R.raw.get_out),
                SoundItem(name = "Wat da HAILLL", resId = R.raw.wait_what_the_hail),
                SoundItem(name = "Wobbly Wiggly", resId = R.raw.wobbly_wiggly),
                SoundItem(name = "John CENAA!", resId = R.raw.john_cena),
                SoundItem(name = "Yo Phone Linging", resId = R.raw.yo_phone_linging),
                SoundItem(name = "Kim Jong Goon", resId = R.raw.kim_jong_goon),
                SoundItem(name = "Prowler", resId = R.raw.prowler)
            )
        }
    }

    // Save sounds
    fun saveSounds(list: List<SoundItem>) {
        val content = list.joinToString("\n") { 
            "${it.id}|${it.name}|${it.resId ?: "null"}|${it.filePath ?: "null"}"
        }
        soundsFile.writeText(content)
    }

    val mediaPlayer = remember {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
    }

    val playSound = { sound: SoundItem ->
        try {
            mediaPlayer.reset()
            if (sound.resId != null) {
                val afd = context.resources.openRawResourceFd(sound.resId)
                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else if (sound.filePath != null) {
                mediaPlayer.setDataSource(sound.filePath)
            }
            
            mediaPlayer.setOnCompletionListener {
                isPlaying.value = false
            }
            
            mediaPlayer.prepare()
            mediaPlayer.setVolume(1.0f, 1.0f)
            mediaPlayer.start()
            isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val stopSound = {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        isPlaying.value = false
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingUri = uri
            showNameDialog = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "MemeBoard",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { isEditMode = !isEditMode }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Done" else "Edit",
                            tint = Color(0xFF90CAF9)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isEditMode,
                enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { pickAudioLauncher.launch("audio/*") },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Sound")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val gridState = rememberLazyGridState()
            
            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sounds, key = { it.id }) { sound ->
                        Box(
                            modifier = Modifier.animateItem(
                                fadeInSpec = tween(300),
                                fadeOutSpec = tween(300),
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        ) {
                            SoundButton(
                                name = sound.name,
                                isEditMode = isEditMode,
                                onRemove = {
                                    sounds = sounds.filter { it.id != sound.id }
                                    saveSounds(sounds)
                                },
                                onClick = {
                                    if (!isEditMode) playSound(sound)
                                }
                            )
                        }
                    }
                }

                // Scrollbar implementation
                if (sounds.isNotEmpty()) {
                    val scrollbarColor = Color(0xFF90CAF9)
                    
                    val thumbHeightFraction by remember {
                        derivedStateOf {
                            val layoutInfo = gridState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val visibleItemsCount = layoutInfo.visibleItemsInfo.size
                            if (totalItems > 0) visibleItemsCount.toFloat() / totalItems else 1f
                        }
                    }

                    val scrollOffsetFraction by remember {
                        derivedStateOf {
                            val layoutInfo = gridState.layoutInfo
                            val totalItems = layoutInfo.totalItemsCount
                            val firstVisibleItem = gridState.firstVisibleItemIndex
                            val firstVisibleItemOffset = gridState.firstVisibleItemScrollOffset
                            val itemHeight = layoutInfo.visibleItemsInfo.firstOrNull()?.size?.height?.toFloat() ?: 1f
                            
                            val totalRows = ceil(totalItems / 2.0).toFloat()
                            val totalHeight = totalRows * itemHeight
                            val viewportHeight = layoutInfo.viewportSize.height.toFloat()
                            
                            val scrollOffset = (firstVisibleItem / 2).toFloat() * itemHeight + firstVisibleItemOffset
                            val maxScroll = (totalHeight - viewportHeight).coerceAtLeast(1f)
                            (scrollOffset / maxScroll).coerceIn(0f, 1f)
                        }
                    }

                    val showScrollbar by remember {
                        derivedStateOf {
                            gridState.layoutInfo.totalItemsCount > gridState.layoutInfo.visibleItemsInfo.size
                        }
                    }

                    if (showScrollbar) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp, top = 16.dp, bottom = 100.dp)
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(scrollbarColor.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(thumbHeightFraction.coerceIn(0.1f, 1f))
                                    .graphicsLayer {
                                        translationY = (size.height * (1f - thumbHeightFraction.coerceIn(0.1f, 1f))) * scrollOffsetFraction
                                    }
                                    .background(scrollbarColor, CircleShape)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isPlaying.value,
                enter = scaleIn() + slideInVertically(initialOffsetY = { it }),
                exit = scaleOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Button(
                    onClick = { stopSound() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252)
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { 
                showNameDialog = false 
                newSoundName = ""
            },
            title = { Text("Sound Name") },
            text = {
                TextField(
                    value = newSoundName,
                    onValueChange = { newSoundName = it },
                    placeholder = { Text("Enter name...") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val uri = pendingUri
                    val currentName = newSoundName
                    if (uri != null && currentName.isNotBlank()) {
                        val mimeType = context.contentResolver.getType(uri)
                        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "mp3"
                        val fileName = "sound_${System.currentTimeMillis()}.$extension"
                        
                        val file = File(context.filesDir, fileName)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        val newItem = SoundItem(name = currentName, filePath = file.absolutePath)
                        val newList = sounds + newItem
                        sounds = newList
                        saveSounds(newList)
                        newSoundName = ""
                        showNameDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showNameDialog = false 
                    newSoundName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SoundButton(
    name: String,
    isEditMode: Boolean,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val randomDurationRotation = remember { (90..120).random() }
    val randomDurationX = remember { (70..110).random() }
    val randomDurationY = remember { (80..130).random() }
    val randomMaxRotation = remember { (10..20).random() / 10f }
    val randomMaxTranslation = remember { (5..15).random() / 10f }

    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -randomMaxRotation,
        targetValue = randomMaxRotation,
        animationSpec = infiniteRepeatable(
            animation = tween(randomDurationRotation, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val translationX by infiniteTransition.animateFloat(
        initialValue = -randomMaxTranslation,
        targetValue = randomMaxTranslation,
        animationSpec = infiniteRepeatable(
            animation = tween(randomDurationX, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translationX"
    )
    val translationY by infiniteTransition.animateFloat(
        initialValue = -randomMaxTranslation,
        targetValue = randomMaxTranslation,
        animationSpec = infiniteRepeatable(
            animation = tween(randomDurationY, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translationY"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer { 
                if (isEditMode) {
                    rotationZ = rotation
                    this.translationX = translationX
                    this.translationY = translationY
                }
            }
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF90CAF9),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }

        AnimatedVisibility(
            visible = isEditMode,
            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
