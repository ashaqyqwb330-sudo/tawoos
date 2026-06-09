package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldenBrass
import com.example.ui.theme.LightNavy
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.CoralRed
import com.example.ui.theme.SoftGray
import com.example.ui.theme.DarkNavy
import kotlinx.coroutines.delay

val HexagonShape = object : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val w = size.width
            val h = size.height
            moveTo(w * 0.5f, 0f)
            lineTo(w, h * 0.25f)
            lineTo(w, h * 0.75f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h * 0.75f)
            lineTo(0f, h * 0.25f)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun HexagonButton(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onTap: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(6.dp)
            .clickable { onTap() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(HexagonShape)
                .background(LightNavy.copy(alpha = 0.25f))
                .border(2.dp, GoldenBrass, HexagonShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SoftGray
            ),
            maxLines = 2
        )
    }
}

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    delayMillis: Long = 10,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.White
) {
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        displayedText = ""
        for (char in text) {
            displayedText += char
            delay(delayMillis)
        }
    }
    Text(
        text = displayedText,
        modifier = modifier,
        style = style,
        color = color,
        lineHeight = 22.sp
    )
}

@Composable
fun RadarWidget(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .size(90.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = 1f - (scale - 0.4f) / 0.9f)
                .background(GoldenBrass.copy(alpha = 0.12f), shape = CircleShape)
                .border(1.dp, GoldenBrass.copy(alpha = 0.25f), shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(45.dp)
                .border(1.dp, GoldenBrass.copy(alpha = 0.5f), shape = CircleShape)
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = androidx.compose.ui.geometry.Offset(cx, cy)
            val radius = minOf(size.width, size.height) / 2f
            drawLine(
                color = GoldenBrass,
                start = center,
                end = androidx.compose.ui.geometry.Offset(cx, cy - radius),
                strokeWidth = 2.5f
            )
        }
    }
}

@Composable
fun MilitaryProgressBar(
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        color = GoldenBrass,
        trackColor = LightNavy.copy(alpha = 0.3f)
    )
}

@Composable
fun ThreatIndicator(
    answer: String,
    modifier: Modifier = Modifier
) {
    val found = !answer.contains("لم يتم العثور") && !answer.contains("حدث خطأ")
    val color = if (found) GoldenBrass else CoralRed
    val label = if (found) "الحالة الأمنية للمستندات: منخفضة التهديد" else "الحالة الأمنية للمستندات: تهديد خارجي (يرجى مراجعة الإدارة)"

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.08f),
        shape = ShapeDefaults.Small,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = color,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
