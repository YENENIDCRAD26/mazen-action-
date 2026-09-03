package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.sound.GameSoundEffects
import com.example.sound.YemeniHeritageRadio
import com.example.sound.YemeniHeritageTrack
import com.example.sound.YemeniMusicCategory
import com.example.ui.theme.*

@Composable
fun YemeniHeritageRadioModal(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf<YemeniMusicCategory?>(null) }
  val activeTrack = YemeniHeritageRadio.currentTrack
  val isPlaying = YemeniHeritageRadio.isPlaying

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(4.dp),
      shape = RoundedCornerShape(22.dp),
      color = DarkSurface,
      border = androidx.compose.foundation.BorderStroke(2.dp, SanaaGold)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🇾🇪", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "راديو التراث والزوامل الصنعانية",
                color = SanaaGold,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = "زوامل قبلية • أغاني حمينية • قصائد شعرية عتيقة",
                color = Color.LightGray,
                fontSize = 10.sp
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Player Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isPlaying) GangNeonGreen else DarkCardBorder, RoundedCornerShape(16.dp))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(activeTrack.iconEmoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = activeTrack.titleAr,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = activeTrack.artistOrPoetAr,
                    color = SanaaGold,
                    fontSize = 10.sp
                  )
                }
              }

              Surface(
                color = if (isPlaying) GangNeonGreen.copy(alpha = 0.2f) else DarkBg,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPlaying) GangNeonGreen else Color.Gray)
              ) {
                Text(
                  text = if (isPlaying) "يعزف الآن 🎵" else "متوقف ⏸️",
                  color = if (isPlaying) GangNeonGreen else Color.Gray,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Currently singing verse display
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Brush.horizontalGradient(listOf(DarkBg, SanaaClay.copy(alpha = 0.35f), DarkBg)))
                .border(1.dp, SanaaGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(10.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "« ${YemeniHeritageRadio.currentVerseText.ifEmpty { activeTrack.verses.first() }} »",
                color = SanaaGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback Controls Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              IconButton(
                onClick = {
                  GameSoundEffects.playDoorCreak()
                  YemeniHeritageRadio.previousTrack()
                },
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(DarkBg)
              ) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "السابق", tint = SanaaGold)
              }

              Spacer(modifier = Modifier.width(16.dp))

              IconButton(
                onClick = {
                  GameSoundEffects.playZamilDrumBeat()
                  YemeniHeritageRadio.togglePlay()
                },
                modifier = Modifier
                  .size(50.dp)
                  .clip(CircleShape)
                  .background(SanaaGold)
                  .testTag("btn_radio_play_toggle")
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                  tint = DarkBg,
                  modifier = Modifier.size(28.dp)
                )
              }

              Spacer(modifier = Modifier.width(16.dp))

              IconButton(
                onClick = {
                  GameSoundEffects.playDoorCreak()
                  YemeniHeritageRadio.nextTrack()
                },
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(DarkBg)
              ) {
                Icon(Icons.Default.SkipNext, contentDescription = "التالي", tint = SanaaGold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Filter Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Surface(
            color = if (selectedCategory == null) SanaaGold else DarkSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .clickable { selectedCategory = null }
          ) {
            Text(
              text = "الكل",
              color = if (selectedCategory == null) DarkBg else Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(vertical = 6.dp)
            )
          }

          YemeniMusicCategory.values().forEach { cat ->
            val isSelected = selectedCategory == cat
            Surface(
              color = if (isSelected) SanaaGold else DarkSurfaceVariant,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1.3f)
                .clickable { selectedCategory = cat }
            ) {
              Text(
                text = "${cat.iconEmoji} ${cat.titleAr.take(7)}",
                color = if (isSelected) DarkBg else Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp),
                maxLines = 1
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playlist Items List
        val filteredList = if (selectedCategory == null) {
          YemeniHeritageRadio.playlist
        } else {
          YemeniHeritageRadio.playlist.filter { it.category == selectedCategory }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          itemsIndexed(filteredList) { index, track ->
            val isThisActive = activeTrack.id == track.id
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isThisActive) SanaaGold.copy(alpha = 0.18f) else DarkBg
              ),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isThisActive) SanaaGold else DarkCardBorder
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val realIdx = YemeniHeritageRadio.playlist.indexOf(track)
                  YemeniHeritageRadio.playTrack(realIdx)
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Text(track.iconEmoji, fontSize = 18.sp)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = track.titleAr,
                      color = if (isThisActive) SanaaGold else Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                    Text(
                      text = "${track.category.titleAr} • ${track.artistOrPoetAr.take(24)}",
                      color = Color.LightGray,
                      fontSize = 9.sp,
                      maxLines = 1
                    )
                  }
                }

                if (isThisActive && isPlaying) {
                  Text("🎶 يعزف", color = GangNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else {
                  Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = SanaaGold,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
