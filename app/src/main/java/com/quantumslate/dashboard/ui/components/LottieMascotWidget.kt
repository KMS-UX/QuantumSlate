package com.quantumslate.dashboard.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.quantumslate.dashboard.data.local.MascotStateEntity

/**
 * Animated mascot widget using Lottie animations.
 * Displays different animations based on mascot mood and character.
 */
@Composable
fun LottieMascotWidget(
    mascotState: MascotStateEntity?,
    modifier: Modifier = Modifier,
    size: Float = 120f
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mascotState == null) {
            // Default fallback - show robot emoji
            Text(
                text = "🤖",
                style = MaterialTheme.typography.displayMedium
            )
        } else {
            // Determine animation resource based on character and mood
            val animationResId = getAnimationForState(mascotState)
            
            if (animationResId != null) {
                val composition by rememberLottieComposition(
                    spec = LottieCompositionSpec.RawRes(animationResId)
                )
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = true
                )
                
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(size.dp)
                )
            } else {
                // Fallback to emoji if no animation found
                val mascotChar = when (mascotState.character.lowercase()) {
                    "cat" -> "🐱"
                    "bird" -> "🐦"
                    "creature" -> "👾"
                    else -> "🤖"
                }
                Text(
                    text = mascotChar,
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }
    }
}

/**
 * Maps mascot state to appropriate Lottie animation resource.
 * Returns null if no animation is available (fallback to emoji).
 */
@Composable
private fun getAnimationForState(state: MascotStateEntity): Int? {
    val character = state.character.lowercase()
    val mood = state.mood.lowercase()
    val animation = state.animation.lowercase()
    
    // Animation naming convention: {character}_{mood_or_animation}.json
    // Examples: robot_happy_wave.json, cat_dancing.json, bird_idle.json
    
    return when {
        // Robot animations
        character == "robot" && animation.contains("wave") -> com.quantumslate.dashboard.R.raw.robot_happy_wave
        character == "robot" && animation.contains("dance") -> com.quantumslate.dashboard.R.raw.robot_dancing
        character == "robot" && animation.contains("worried") -> com.quantumslate.dashboard.R.raw.robot_worried
        character == "robot" && animation.contains("sleep") -> com.quantumslate.dashboard.R.raw.robot_sleeping
        character == "robot" -> com.quantumslate.dashboard.R.raw.robot_idle
        
        // Cat animations
        character == "cat" && (mood == "happy" || animation.contains("happy")) -> com.quantumslate.dashboard.R.raw.cat_happy
        character == "cat" && animation.contains("dance") -> com.quantumslate.dashboard.R.raw.cat_dancing
        character == "cat" && animation.contains("worried") -> com.quantumslate.dashboard.R.raw.cat_concerned
        character == "cat" && animation.contains("sleep") -> com.quantumslate.dashboard.R.raw.cat_sleepy
        character == "cat" -> com.quantumslate.dashboard.R.raw.cat_idle
        
        // Bird animations
        character == "bird" && (mood == "happy" || animation.contains("happy")) -> com.quantumslate.dashboard.R.raw.bird_happy
        character == "bird" && animation.contains("dance") -> com.quantumslate.dashboard.R.raw.bird_excited
        character == "bird" && animation.contains("worried") -> com.quantumslate.dashboard.R.raw.bird_worried
        character == "bird" && animation.contains("sleep") -> com.quantumslate.dashboard.R.raw.bird_sleeping
        character == "bird" -> com.quantumslate.dashboard.R.raw.bird_idle
        
        // Creature animations
        character == "creature" && (mood == "happy" || animation.contains("happy")) -> com.quantumslate.dashboard.R.raw.creature_happy
        character == "creature" && animation.contains("dance") -> com.quantumslate.dashboard.R.raw.creature_dancing
        character == "creature" && animation.contains("worried") -> com.quantumslate.dashboard.R.raw.creature_worried
        character == "creature" && animation.contains("sleep") -> com.quantumslate.dashboard.R.raw.creature_sleeping
        character == "creature" -> com.quantumslate.dashboard.R.raw.creature_idle
        
        else -> null
    }
}

/**
 * Lightweight version of the mascot widget that uses simple animated emojis.
 * Useful as a fallback or for performance-critical scenarios.
 */
@Composable
fun AnimatedEmojiMascot(
    mascotState: MascotStateEntity?,
    modifier: Modifier = Modifier,
    size: Float = 100f
) {
    var emojiScale by remember { mutableStateOf(1f) }
    
    LaunchedEffect(mascotState?.mood) {
        // Simple breathing animation
        while (true) {
            kotlinx.coroutines.delay(1000)
            emojiScale = if (emojiScale == 1f) 1.1f else 1f
        }
    }
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        if (mascotState == null) {
            Text(
                text = "🤖",
                style = MaterialTheme.typography.displayMedium
            )
        } else {
            val mascotChar = when (mascotState.character.lowercase()) {
                "cat" -> "🐱"
                "bird" -> "🐦"
                "creature" -> "👾"
                else -> "🤖"
            }
            
            val accessory = when (mascotState.mood.lowercase()) {
                "happy" -> "😊"
                "excited" -> "🎉"
                "concerned" -> "😟"
                "sleepy" -> "💤"
                else -> ""
            }
            
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = mascotChar,
                    style = MaterialTheme.typography.displayMedium
                )
                if (accessory.isNotEmpty()) {
                    Text(
                        text = accessory,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}
