# LocalFlow - Liquid Glass UI Specification & Implementation Guide (Kotlin / Jetpack Compose)

This document provides complete color palettes, custom Jetpack Compose modifiers, reusable glass components (Buttons, Lens Switches, Navigation Bars, Cards), and an AI Agent Master Prompt to upgrade the **LocalFlow** Android App to an ultra-realistic Liquid Glass UI.

---

## 1. Design Tokens & Color Palette

### Dark Liquid AMOLED Theme (Primary for LocalFlow)
- **Background Base:** `#0D0F14` (Deep Space Dark) / `#000000` (AMOLED True Black)
- **Glass Panel Surface (Dark):** `LinearGradient(0.12 opacity White -> 0.04 opacity White)`
- **Glass Border Reflection (Specular Highlight):** `LinearGradient(0.35 opacity White -> 0.05 opacity White)`
- **Cyan Liquid Glow Accent:** `#00E5FF` / `#0284C7`
- **Purple Refraction Accent:** `#A855F7` / `#7E22CE`
- **Inner Shadow/Glow:** `rgba(255, 255, 255, 0.15)` top edge highlight

### Light Crystal Glass Theme
- **Background Base:** `#EBF1F5` (Soft Ice White)
- **Glass Panel Surface (Light):** `LinearGradient(0.60 opacity White -> 0.20 opacity White)`
- **Glass Border Reflection:** `LinearGradient(0.90 opacity White -> 0.30 opacity White)`
- **Glass Drop Shadow:** `rgba(0, 0, 0, 0.08)` blur 20dp

---

## 2. Core Kotlin Jetpack Compose Glass Components

### A. Custom Liquid Glass Modifier (`Modifier.liquidGlass`)
This custom modifier applies backdrop surface shading, top specular highlight border, and subtle inner glow.

```kotlin
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CornerSize
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.liquidGlass(
    shape: Shape,
    isDark: Boolean = true,
    borderColor: Color = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.8f),
    glassAlpha: Float = if (isDark) 0.15f else 0.45f
): Modifier = this.drawBehind {
    // Glass Surface Background Gradient
    val glassBg = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = glassAlpha),
            Color.White.copy(alpha = glassAlpha * 0.3f)
        )
    )
    
    // Specular Rim Highlight (Refraction edge)
    val specularRim = Brush.verticalGradient(
        colors = listOf(
            borderColor,
            borderColor.copy(alpha = 0.05f)
        )
    )

    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        brush = glassBg
    )
    
    drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        brush = specularRim,
        style = Stroke(width = 1.5.dp.toPx())
    )
}
```

---

### B. Liquid Lens Toggle Switch (Matching Images #1 & #2)
The iconic lens slider with sliding glass bulb and dynamic light/dark refraction.

```kotlin
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiquidLensSwitch(
    isDarkMode: Boolean,
    onModeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackWidth = 140.dp
    val lensSize = 48.dp
    
    val lensOffset by animateDpAsState(
        targetValue = if (isDarkMode) (trackWidth - lensSize - 4.dp) else 4.dp,
        animationSpec = tween(durationMillis = 400),
        label = "LensSlideAnimation"
    )

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .liquidGlass(RoundedCornerShape(28.dp), isDark = isDarkMode)
            .clickable { onModeChanged(!isDarkMode) }
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Labels
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Light",
                color = if (!isDarkMode) Color.White else Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dark",
                color = if (isDarkMode) Color.White else Color.Black.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Sliding High-Refraction Glass Lens Bulb
        Box(
            modifier = Modifier
                .offset(x = lensOffset)
                .size(lensSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    )
                )
                .liquidGlass(CircleShape, isDark = isDarkMode, borderColor = Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            val iconColor = if (isDarkMode) Color(0xFF818CF8) else Color(0xFFF59E0B)
            // Icon dynamically switches
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(iconColor, CircleShape)
            )
        }
    }
}
```

---

### C. Specular Glass Button (`LiquidGlassButton`)

```kotlin
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF),
    icon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        accentColor.copy(alpha = 0.15f)
                    )
                )
            )
            .liquidGlass(RoundedCornerShape(25.dp), borderColor = accentColor.copy(alpha = 0.6f))
            .clickable { onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.invoke()
            if (icon != null) Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
```

---

### D. Glass Video Card (`GlassVideoCard`) for LocalFlow Video Items

```kotlin
@Composable
fun GlassVideoCard(
    title: String,
    duration: String,
    qualityBadge: String = "1080p",
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .liquidGlass(RoundedCornerShape(20.dp), isDark = true)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Thumbnail Glass Container
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Quality Tag Badge (Top Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = qualityBadge,
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Duration Glass Pill (Bottom Right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .liquidGlass(RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = duration,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Video Title Header
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
```

---

## 3. Master Prompt for AI Coding Agent (Cursor / Android Studio Copilot)

Copy and paste the prompt below into your AI Coding Agent to convert your current Kotlin codebase to Liquid Glass UI automatically:

```text
PROMPT FOR AI AGENT:

System Goal: Refactor the entire UI layer of the LocalFlow Android App (written in Kotlin Jetpack Compose / XML) to adopt a hyper-realistic "Liquid Glass UI" design system inspired by visionOS.

Key UI Requirements & Specifications:
1. Liquid Glass Panels: Apply dynamic multi-layer white specular gradient rims (1.5dp Stroke with Brush.verticalGradient), backdrop blur, and semi-transparent dark backgrounds (Color.White with 0.08f to 0.18f alpha).
2. Refraction Lens Switch: Replace the current Theme Switch with a custom Liquid Lens Switch component featuring a smooth sliding circular glass bulb and specular edge glint.
3. Media Cards & Controls: Style all video thumbnails, bottom navigation bars, top headers, player controls (play, pause, seekbar), search inputs, and dropdowns with rounded pill/capsule shapes and liquid glass modifiers.
4. Accent Color Scheme:
   - Primary Glow: #00E5FF (Cyan Liquid)
   - Secondary Glow: #A855F7 (Neon Purple Glass)
   - Dark Background: #0D0F14 with ambient radial gradient blobs behind video lists.
5. Smooth Physics & Animations: Ensure touch ripples use liquid press scaling (scale 0.97f on press) with spring animation specs.

Please update all Compose screens (Home, Shorts, Player, Settings, Video Library Cards, Bottom Bar) to integrate these Liquid Glass components.
```