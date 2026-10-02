package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoyalBlue600
import com.example.ui.viewmodel.NavigationTab
import kotlin.math.min

/**
 * G2 Continuous Curvature Squircle Shape.
 * Produces organic, continuous-curvature corners (eliminating curvature step-discontinuities)
 * with a customizable decreased corner radius for modern, refined geometry.
 */
class G2SquircleShape(
    val cornerRadius: Dp = 20.dp,
    val smoothness: Float = 0.44f // G2 curvature control factor
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radiusPx = with(density) { cornerRadius.toPx() }
        val maxRadius = min(size.width, size.height) / 2f
        val r = min(radiusPx, maxRadius)

        if (r <= 0f) {
            return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))
        }

        val w = size.width
        val h = size.height
        val c = smoothness.coerceIn(0.2f, 0.55f)

        val path = Path().apply {
            // Start at top-left edge after corner
            moveTo(r, 0f)

            // Top straight edge
            lineTo(w - r, 0f)

            // Top-Right G2 Squircle corner
            cubicTo(
                x1 = w - r * c,
                y1 = 0f,
                x2 = w,
                y2 = r - r * c,
                x3 = w,
                y3 = r
            )

            // Right straight edge
            lineTo(w, h - r)

            // Bottom-Right G2 Squircle corner
            cubicTo(
                x1 = w,
                y1 = h - r * c,
                x2 = w - r * c,
                y2 = h,
                x3 = w - r,
                y3 = h
            )

            // Bottom straight edge
            lineTo(r, h)

            // Bottom-Left G2 Squircle corner
            cubicTo(
                x1 = r * c,
                y1 = h,
                x2 = 0f,
                y2 = h - r * c,
                x3 = 0f,
                y3 = h - r
            )

            // Left straight edge
            lineTo(0f, r)

            // Top-Left G2 Squircle corner
            cubicTo(
                x1 = 0f,
                y1 = r * c,
                x2 = r * c,
                y2 = 0f,
                x3 = r,
                y3 = 0f
            )

            close()
        }

        return Outline.Generic(path)
    }
}

/**
 * Liquid Glass Floating Bottom Navigation Dock with G2 Squircle geometry,
 * decreased corner radius, multi-layer chromatic refraction, top specular glare line,
 * and high liquid glassmorphism.
 */
@Composable
fun LiquidGlassBottomNav(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
    savedCount: Int = 0
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f // Accurate detection for custom themes
    val primaryColor = MaterialTheme.colorScheme.primary

    // Decreased radius G2 Squircle: 20.dp for sleek dock silhouette
    val dockShape = remember { G2SquircleShape(cornerRadius = 20.dp, smoothness = 0.44f) }

    // Liquid Glass Substrate Gradient
    val glassBackground = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xF0101726), // 94% opacity deep indigo-slate
                Color(0xDB0C1322), // 86% opacity mid-tone refraction
                Color(0xEB080E1A)  // 92% opacity bottom glass bed
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xF5FFFFFF), // 96% opacity crystalline frosted white
                Color(0xE6F8FAFC), // 90% opacity soft silver mist
                Color(0xEDF1F5F9)  // 93% opacity polished light base
            )
        )
    }

    // High Liquid Specular Edge Rim (Top-lit refraction)
    val glassRimBorder = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.68f), // Crisp top light reflection
                Color.White.copy(alpha = 0.22f), // Soft translucent flank
                NeonCyan.copy(alpha = 0.18f),   // Subtle cyan chromatic dispersion
                Color.White.copy(alpha = 0.08f)  // Base rim
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.95f), // High-gloss daylight reflection
                Color.White.copy(alpha = 0.45f),
                RoyalBlue600.copy(alpha = 0.20f),
                Color(0x33CBD5E1)
            )
        )
    }

    // Liquid Chromatic Glow Ambient Spot
    val spotColor = if (isDark) NeonCyan.copy(alpha = 0.30f) else RoyalBlue600.copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_navigation_bar")
            .shadow(
                elevation = 18.dp,
                shape = dockShape,
                spotColor = spotColor,
                ambientColor = Color.Black.copy(alpha = if (isDark) 0.50f else 0.18f)
            )
            .clip(dockShape)
            .background(glassBackground)
            .border(
                BorderStroke(1.2.dp, glassRimBorder),
                shape = dockShape
            )
            // Liquid Glass Internal Specular Glare & Refraction Layer
            .drawBehind {
                // 1. Top Specular Glare Line (Simulates physical light refracting on the curved glass lip)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.55f else 0.85f),
                            Color.White.copy(alpha = if (isDark) 0.80f else 1.0f),
                            Color.White.copy(alpha = if (isDark) 0.55f else 0.85f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(x = 16.dp.toPx(), y = 1.dp.toPx()),
                    size = Size(width = size.width - 32.dp.toPx(), height = 1.6.dp.toPx())
                )

                // 2. Diagonal Gloss Sheen across the glass surface
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.06f else 0.12f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (isDark) 0.03f else 0.08f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )

                // 3. Subtle ambient chromatic core refraction
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.09f else 0.06f),
                            Color.Transparent
                        ),
                        radius = size.width * 0.45f,
                        center = Offset(size.width * 0.5f, size.height * 0.8f)
                    )
                )
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Discover / Home
            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.HOME,
                onClick = { onTabSelected(NavigationTab.HOME) },
                icon = if (currentTab == NavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                label = "Discover",
                tag = "tab_home",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Tab 2: Events
            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.EVENTS,
                onClick = { onTabSelected(NavigationTab.EVENTS) },
                icon = if (currentTab == NavigationTab.EVENTS) Icons.Filled.Explore else Icons.Outlined.Explore,
                label = "Events",
                tag = "tab_events",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Tab 3: Clubs
            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.CLUBS,
                onClick = { onTabSelected(NavigationTab.CLUBS) },
                icon = if (currentTab == NavigationTab.CLUBS) Icons.Filled.Groups else Icons.Outlined.Groups,
                label = "Clubs",
                tag = "tab_clubs",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Tab 4: Saved (with dynamic badge)
            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.SAVED,
                onClick = { onTabSelected(NavigationTab.SAVED) },
                icon = if (currentTab == NavigationTab.SAVED) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                label = "Saved",
                tag = "tab_saved",
                isDark = isDark,
                badgeCount = savedCount,
                modifier = Modifier.weight(1f)
            )

            // Tab 5: Settings (Account, Profile, Data, Features, About & Logout)
            LiquidGlassNavItem(
                selected = currentTab == NavigationTab.SETTINGS || currentTab == NavigationTab.PROFILE,
                onClick = { onTabSelected(NavigationTab.SETTINGS) },
                icon = if (currentTab == NavigationTab.SETTINGS || currentTab == NavigationTab.PROFILE) Icons.Filled.Settings else Icons.Outlined.Settings,
                label = "Settings",
                tag = "tab_settings",
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual Liquid Glass Navigation Item with G2 Squircle active indicator,
 * decreased corner radius, smooth spring micro-animations, and liquid depth.
 */
@Composable
private fun LiquidGlassNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    tag: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Active Tab Indicator: Decreased radius G2 Squircle (13.dp)
    val itemSquircleShape = remember { G2SquircleShape(cornerRadius = 13.dp, smoothness = 0.44f) }

    // Micro-interactions: scale bounce and color transition
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) primaryColor else onSurfaceVariant.copy(alpha = 0.70f),
        label = "contentColor"
    )

    val activeGlassFill = if (isDark) {
        Brush.verticalGradient(
            listOf(
                primaryColor.copy(alpha = 0.26f),
                primaryColor.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                primaryColor.copy(alpha = 0.20f),
                primaryColor.copy(alpha = 0.08f)
            )
        )
    }

    val activeRimStroke = if (isDark) {
        Brush.verticalGradient(
            listOf(
                primaryColor.copy(alpha = 0.65f),
                primaryColor.copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                primaryColor.copy(alpha = 0.50f),
                primaryColor.copy(alpha = 0.18f)
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .testTag(tag)
            .clip(itemSquircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = primaryColor),
                onClick = onClick
            )
            .then(
                if (selected) {
                    Modifier
                        .background(activeGlassFill, shape = itemSquircleShape)
                        .border(BorderStroke(1.dp, activeRimStroke), shape = itemSquircleShape)
                        .drawBehind {
                            // Subtle liquid gleam inside active tab
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = if (isDark) 0.35f else 0.60f),
                                        Color.Transparent
                                    )
                                ),
                                topLeft = Offset(8.dp.toPx(), 1.dp.toPx()),
                                size = Size(size.width - 16.dp.toPx(), 1.dp.toPx())
                            )
                        }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with badge container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(iconScale)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )

                // Optional vibrant notification/saved badge
                if (badgeCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor,
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF0B0F17) else Color.White),
                        modifier = Modifier
                            .size(7.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
