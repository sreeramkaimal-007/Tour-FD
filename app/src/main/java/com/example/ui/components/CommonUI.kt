package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attractions
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ExpenseCategory

fun parseHexColor(hex: String, fallback: Color = Color(0xFF0D5C75)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or colorInt)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        fallback
    }
}

fun getCategoryIcon(cat: ExpenseCategory): ImageVector {
    return when (cat) {
        ExpenseCategory.FOOD -> Icons.Default.Restaurant
        ExpenseCategory.STAY -> Icons.Default.Hotel
        ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
        ExpenseCategory.ACTIVITY -> Icons.Default.Attractions
        ExpenseCategory.GROCERY -> Icons.Default.LocalGroceryStore
        ExpenseCategory.SHOPPING -> Icons.Default.ShoppingBag
        ExpenseCategory.MISC -> Icons.Default.ReceiptLong
    }
}

@Composable
fun MemberAvatar(
    name: String,
    avatarColorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    val bgColor = parseHexColor(avatarColorHex)
    val initials = name.trim().split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "U" }

    Box(
        modifier = modifier
            .size(size)
            .background(bgColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontSize = (size.value * 0.4).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryIconBadge(
    category: ExpenseCategory,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val catColor = parseHexColor(category.colorHex)
    Box(
        modifier = modifier
            .size(size)
            .background(catColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = category.displayName,
            tint = catColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
