package com.bluefirestudios.memeboard

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bluefirestudios.memeboard.ui.theme.MemeBoardTheme

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

data class SoundItem(val name: String, val resId: Int)

@Composable
fun SoundboardScreen() {
    val context = LocalContext.current
    val resources = context.resources
    
    val sounds = remember {
        listOf(
            SoundItem("Dexter", R.raw.dexter),
            SoundItem("Bruh", R.raw.bruh),
            SoundItem("Congrats Afghan", R.raw.congratulations),
            SoundItem("Dial Up", R.raw.dial_up),
            SoundItem("Galaxy Meme", R.raw.galaxy_meme),
            SoundItem("Gay Echo", R.raw.gay_echo),
            SoundItem("Gop Gop Gop", R.raw.gopgopgop),
            SoundItem("Lego Breaking", R.raw.lego_breaking),
            SoundItem("FAHHHHH", R.raw.fah),
            SoundItem("Max Verstappen", R.raw.max_verstrappen),
            SoundItem("Jet2 Holiday", R.raw.jet2holiday),
            SoundItem("SIXX SEVENN", R.raw.six_seven),
            SoundItem("Vine Boom", R.raw.vine_boom),
            SoundItem("Oh My God 1", R.raw.oh_my_god),
            SoundItem("Oh My God 2", R.raw.oh_my_god2),
            SoundItem("GET OUT", R.raw.get_out),
            SoundItem("Wat da HAILLL", R.raw.wait_what_the_hail),
            SoundItem("Wobbly Wiggly", R.raw.wobbly_wiggly),
            SoundItem("John CENAA!", R.raw.john_cena),
            SoundItem("Yo Phone Linging", R.raw.yo_phone_linging),
            SoundItem("Windows 95", R.raw.windows95),
            SoundItem("Prowler", R.raw.prowler)
        )
    }

    // Optimization: Reuse a single MediaPlayer instance
    // Changed AudioAttributes to USAGE_MEDIA to use the Media volume stream instead of System/Notification
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

    val isPlaying = remember { mutableStateOf(false) }

    val playSound = { resId: Int ->
        try {
            mediaPlayer.reset()
            val afd = resources.openRawResourceFd(resId)
            mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            
            mediaPlayer.setOnCompletionListener {
                isPlaying.value = false
            }
            
            mediaPlayer.prepare()
            mediaPlayer.setVolume(1.0f, 1.0f) // Ensure volume is set to maximum for the stream
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MemeBoard",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sounds) { sound ->
                        SoundButton(name = sound.name) {
                            playSound(sound.resId)
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
}

@Composable
fun SoundButton(name: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF90CAF9),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
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
}

@Preview(showBackground = true)
@Composable
fun SoundboardPreview() {
    MemeBoardTheme {
        SoundboardScreen()
    }
}
