package com.subhunt.app.ui.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val angle: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun CelebrationOverlay(
    show: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    if (!show) return

    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = -0.1f,
                color = listOf(
                    Color(0xFFFF6B6B),
                    Color(0xFF4ECDC4),
                    Color(0xFFFFE66D),
                    Color(0xFF95E1D3),
                    Color(0xFFF38181),
                    Color(0xFFAA96DA),
                    Color(0xFFFCBDAD)
                ).random(),
                size = Random.nextFloat() * 8f + 4f,
                speed = Random.nextFloat() * 2f + 1f,
                angle = Random.nextFloat() * 360f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 10f - 5f
            )
        }
    }

    val animationProgress = remember { Animatable(0f) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show && !finished) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 2000,
                    easing = FastOutSlowInEasing
                )
            )
            finished = true
            onFinished()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val progress = animationProgress.value
        particles.forEach { particle ->
            drawConfetti(progress, particle)
        }
    }
}

private fun DrawScope.drawConfetti(progress: Float, particle: ConfettiParticle) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val x = particle.x * canvasWidth + cos(Math.toRadians(particle.angle.toDouble())).toFloat() * progress * 100f
    val y = particle.y * canvasHeight + progress * canvasHeight * particle.speed

    if (y > canvasHeight * 1.2f) return

    val alpha = (1f - progress).coerceIn(0f, 1f)

    drawCircle(
        color = particle.color.copy(alpha = alpha),
        radius = particle.size * (1f - progress * 0.5f),
        center = Offset(x, y)
    )
}

object CelebrationEffects {
    val successColors = listOf(
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
        Color(0xFFCDDC39),
        Color(0xFFFFEB3B),
        Color(0xFFFFC107)
    )

    val celebrationColors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF95E1D3),
        Color(0xFFF38181),
        Color(0xFFAA96DA),
        Color(0xFFFCBDAD)
    )
}
