package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sound.ChaseProximityIntensity
import com.example.sound.GameSoundEffects
import com.example.sound.SanaaAmbientSoundManager
import com.example.sound.YemeniAmbientTrack
import com.example.ui.theme.*

/**
 * Interactive Modal for the Sana'a Ambient Sound & Music Intensity Manager.
 * Displays live chase proximity, tempo shifts, traditional Yemeni tracks, and playback controls.
 */
@Composable
fun SanaaAmbientSoundModal(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentTrack by SanaaAmbientSoundManager.currentTrack.collectAsState()
  val activeTrackIdx by SanaaAmbientSoundManager.currentTrackIndex.collectAsState()
  val intensityState by SanaaAmbientSoundManager.intensityState.collectAsState()
  val currentBpm by SanaaAmbientSoundManager.currentBpm.collectAsState()
  val intensityRatio by SanaaAmbientSoundManager.intensityRatio.collectAsState()
  val proximityMeters by SanaaAmbientSoundManager.proximityDistanceMeters.collectAsState()
  val isMusicPlaying by SanaaAmbientSoundManager.isMusicPlaying.collectAsState()
  val beatTick by SanaaAmbientSoundManager.beatPulseTick.collectAsState()
  val currentVerseIdx by SanaaAmbientSoundManager.currentVerseIndex.collectAsState()
  var isMuted by remember { mutableStateOf(GameSoundEffects.isMuted) }

  val tracks = SanaaAmbientSoundManager.tracks

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(4.dp)
        .testTag("sanaa_ambient_sound_modal"),
      shape = RoundedCornerShape(24.dp),
      color = DarkSurface,
      border = BorderStroke(2.dp, Color(intensityState.colorHex))
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Modal Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🇾🇪", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "موسيقى وإيقاعات صنعاء التفاعلية",
                color = SanaaGold,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
              )
              Text(
                text = "تتغير شدة وسرعة الإيقاع ديناميكياً مع اقتراب الشرطة",
                color = Color.LightGray,
                fontSize = 9.sp
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(30.dp).testTag("close_ambient_sound_modal_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Chase Proximity & Intensity Real-Time Meter
        Surface(
          color = Color(0xFF0D1B2A),
          shape = RoundedCornerShape(16.dp),
          border = BorderStroke(1.5.dp, Color(intensityState.colorHex)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(intensityState.badgeEmoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = intensityState.titleAr,
                    color = Color(intensityState.colorHex),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                  )
                  Text(
                    text = intensityState.urgencyLabelAr,
                    color = Color.LightGray,
                    fontSize = 8.5.sp
                  )
                }
              }

              Surface(
                color = Color(intensityState.colorHex).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(intensityState.colorHex))
              ) {
                Text(
                  text = "⚡ $currentBpm BPM",
                  color = Color(intensityState.colorHex),
                  fontWeight = FontWeight.Black,
                  fontSize = 11.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Dancing Audio Equalizer Bars
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFF050A10), RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.Bottom
            ) {
              val barCount = 12
              for (i in 0 until barCount) {
                val waveOffset = (i * 0.4f + beatTick * 0.5f)
                val baseHeight = (0.25f + 0.65f * kotlin.math.abs(kotlin.math.sin(waveOffset.toDouble())).toFloat())
                val dynamicHeight = (baseHeight * (0.3f + intensityRatio * 0.7f)).coerceIn(0.15f, 1.0f)
                Box(
                  modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(dynamicHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                      Brush.verticalGradient(
                        listOf(
                          Color(intensityState.colorHex),
                          SanaaGold
                        )
                      )
                    )
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "مسافة أقرب دورية: ${proximityMeters.toInt()} متر",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "مستوى التوتر: ${(intensityRatio * 100).toInt()}%",
                color = Color(intensityState.colorHex),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Track Showcase & Lyrics
        Surface(
          color = DarkBg.copy(alpha = 0.90f),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, SanaaGold.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(currentTrack.iconEmoji, fontSize = 20.sp)
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = currentTrack.titleAr,
                color = SanaaGold,
                fontWeight = FontWeight.Black,
                fontSize = 12.5.sp
              )
            }

            Text(
              text = "${currentTrack.categoryAr} • ${currentTrack.poetOrOriginAr}",
              color = Color.LightGray,
              fontSize = 8.5.sp
            )

            if (currentTrack.versesAr.isNotEmpty()) {
              Spacer(modifier = Modifier.height(6.dp))
              val verse = currentTrack.versesAr[currentVerseIdx % currentTrack.versesAr.size]
              Surface(
                color = Color(0x33F59E0B),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "« $verse »",
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center,
                  lineHeight = 14.sp,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Playback Controls Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = {
                  SanaaAmbientSoundManager.previousTrack()
                  GameSoundEffects.playOudNote(392.0, 150)
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", tint = Color.White)
              }

              IconButton(
                onClick = {
                  if (isMusicPlaying) {
                    SanaaAmbientSoundManager.pauseAmbientMusic()
                  } else {
                    SanaaAmbientSoundManager.startAmbientMusic()
                  }
                  GameSoundEffects.playRadioBeep()
                },
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(SanaaGold)
              ) {
                Icon(
                  imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = "تشغيل/إيقاف",
                  tint = DarkBg,
                  modifier = Modifier.size(24.dp)
                )
              }

              IconButton(
                onClick = {
                  SanaaAmbientSoundManager.nextTrack()
                  GameSoundEffects.playOudNote(440.0, 150)
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(Icons.Default.SkipNext, contentDescription = "التالي", tint = Color.White)
              }

              IconButton(
                onClick = {
                  val newMuted = SanaaAmbientSoundManager.toggleMute()
                  GameSoundEffects.isMuted = newMuted
                  isMuted = newMuted
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = "كتم الصوت",
                  tint = if (isMuted) Color.Red else Color.White
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Track List
        Text(
          text = "🎵 المقاطع التراثية الصنعانية المتاحة:",
          color = SanaaGold,
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 160.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          itemsIndexed(tracks) { idx, track ->
            val isSelected = idx == activeTrackIdx
            Surface(
              color = if (isSelected) SanaaGold.copy(alpha = 0.20f) else DarkBg.copy(alpha = 0.70f),
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = if (isSelected) SanaaGold else Color(0x33FFFFFF)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  SanaaAmbientSoundManager.selectTrack(idx)
                  GameSoundEffects.playOudNote(349.23, 160)
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(track.iconEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = track.titleAr,
                    color = if (isSelected) SanaaGold else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                  Text(
                    text = track.categoryAr,
                    color = Color.LightGray,
                    fontSize = 8.5.sp
                  )
                }

                if (isSelected) {
                  Surface(
                    color = SanaaGold,
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = "شغال 🎶",
                      color = DarkBg,
                      fontWeight = FontWeight.Black,
                      fontSize = 8.sp,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = SanaaGold),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
        ) {
          Text("العودة لأزقة صنعاء", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
      }
    }
  }
}
