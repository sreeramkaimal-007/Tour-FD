package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MemberEntity
import com.example.domain.model.ExpenseCategory
import com.example.domain.model.SplitType
import java.util.Locale

data class CurrencyItem(val symbol: String, val code: String, val name: String)

val INTERNATIONAL_CURRENCIES = listOf(
    CurrencyItem("$", "USD", "US Dollar ($)"),
    CurrencyItem("€", "EUR", "Euro (€)"),
    CurrencyItem("£", "GBP", "British Pound (£)"),
    CurrencyItem("₹", "INR", "Indian Rupee (₹)"),
    CurrencyItem("¥", "JPY", "Japanese Yen (¥)"),
    CurrencyItem("CA$", "CAD", "Canadian Dollar (CA$)"),
    CurrencyItem("AU$", "AUD", "Australian Dollar (AU$)"),
    CurrencyItem("CHF", "CHF", "Swiss Franc (CHF)"),
    CurrencyItem("AED", "AED", "UAE Dirham (AED)"),
    CurrencyItem("SG$", "SGD", "Singapore Dollar (SG$)"),
    CurrencyItem("CN¥", "CNY", "Chinese Yuan (CN¥)"),
    CurrencyItem("RM", "MYR", "Malaysian Ringgit (RM)"),
    CurrencyItem("฿", "THB", "Thai Baht (฿)"),
    CurrencyItem("NZ$", "NZD", "New Zealand Dollar (NZ$)"),
    CurrencyItem("R$", "BRL", "Brazilian Real (R$)"),
    CurrencyItem("R", "ZAR", "South African Rand (R)"),
    CurrencyItem("₩", "KRW", "South Korean Won (₩)"),
    CurrencyItem("MX$", "MXN", "Mexican Peso (MX$)"),
    CurrencyItem("SAR", "SAR", "Saudi Riyal (SAR)"),
    CurrencyItem("₺", "TRY", "Turkish Lira (₺)"),
    CurrencyItem("kr", "SEK", "Swedish Krona (kr)"),
    CurrencyItem("zł", "PLN", "Polish Zloty (zł)"),
    CurrencyItem("₱", "PHP", "Philippine Peso (₱)"),
    CurrencyItem("Rp", "IDR", "Indonesian Rupiah (Rp)"),
    CurrencyItem("₫", "VND", "Vietnamese Dong (₫)"),
    CurrencyItem("E£", "EGP", "Egyptian Pound (E£)"),
    CurrencyItem("KD", "KWD", "Kuwaiti Dinar (KD)"),
    CurrencyItem("QR", "QAR", "Qatari Riyal (QR)")
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTourDialog(
    isSubscribed: Boolean,
    onDismiss: () -> Unit,
    onUpgradeRequired: () -> Unit,
    onConfirm: (
        title: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        currencySymbol: String,
        budget: Double,
        themeColorHex: String,
        members: List<Pair<String, String>>,
        organizerIsFamily: Boolean,
        organizerWeight: Float,
        organizerFamilyName: String,
        organizerFamilyMembers: String,
        organizerKidsInfo: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var currencySymbol by remember { mutableStateOf("$") }
    var customCurrencyInput by remember { mutableStateOf("") }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var budgetStr by remember { mutableStateOf("1500") }
    var selectedThemeColor by remember { mutableStateOf("#0D5C75") }

    // Organizer (Profile Owner) setup: Solo Member vs Family
    var organizerIsFamily by remember { mutableStateOf(false) }
    var organizerFamilyName by remember { mutableStateOf("") }
    var organizerAdultsCount by remember { mutableIntStateOf(2) }
    var organizerKidsCount by remember { mutableIntStateOf(0) }
    val organizerKidAges = remember { mutableStateListOf<Int>() }
    var organizerCustomWeight by remember { mutableStateOf("") }
    var organizerMembersInput by remember { mutableStateOf("") }

    // Start with empty members list
    val membersList = remember { mutableStateListOf<Pair<String, String>>() }

    // Companion Account inputs
    var companionAccountName by remember { mutableStateOf("") }
    var companionIsFamily by remember { mutableStateOf(false) }
    var companionAdultsCount by remember { mutableIntStateOf(2) }
    var companionKidsCount by remember { mutableIntStateOf(0) }
    val companionKidAges = remember { mutableStateListOf<Int>() }
    var companionCustomWeight by remember { mutableStateOf("") }
    var companionMembersInput by remember { mutableStateOf("") }
    var viewingMemberDetailsIdx by remember { mutableStateOf<Int?>(null) }
    var customColorHexInput by remember { mutableStateOf("") }

    val themeColors = listOf(
        "#0D5C75", "#E67E22", "#16A085", "#7B1FA2", "#C2185B", "#388E3C",
        "#1E88E5", "#F4511E", "#00897B", "#8E24AA", "#3949AB", "#D81B60",
        "#43A047", "#FB8C00", "#546E7A", "#263238"
    )
    val popularCurrencies = listOf("$", "€", "£", "₹", "¥", "CA$", "AU$", "CHF", "AED", "SG$")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Plan New Tour & Expenses", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tour Name") },
                    placeholder = { Text("e.g. Summer Vacation, Mountain Trek") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tour_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination") },
                    placeholder = { Text("e.g. Paris, Tokyo, Goa, New York") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // International Currency Picker
                Text("Select Currency (International)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                // Quick chips of top currencies
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    popularCurrencies.take(6).forEach { curr ->
                        val isSelected = currencySymbol == curr && customCurrencyInput.isBlank()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    currencySymbol = curr
                                    customCurrencyInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = curr,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Dropdown for all 28+ international currencies & Custom symbol
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(0.6f)) {
                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = INTERNATIONAL_CURRENCIES.firstOrNull { it.symbol == currencySymbol }?.name ?: "All Currencies ($currencySymbol)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("All Currencies") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                INTERNATIONAL_CURRENCIES.forEach { currItem ->
                                    DropdownMenuItem(
                                        text = { Text("${currItem.code} • ${currItem.name}") },
                                        onClick = {
                                            currencySymbol = currItem.symbol
                                            customCurrencyInput = ""
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customCurrencyInput,
                        onValueChange = {
                            customCurrencyInput = it
                            if (it.isNotBlank()) currencySymbol = it
                        },
                        label = { Text("Custom") },
                        placeholder = { Text("e.g. zł") },
                        modifier = Modifier.weight(0.4f),
                        singleLine = true
                    )
                }

                // Budget
                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Estimated Budget ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Theme Color Palette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tour Theme Color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Swipe for colors 👉", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Smooth horizontally scrollable palette
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    themeColors.forEach { hex ->
                        val isSelected = selectedThemeColor.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedThemeColor = hex
                                    customColorHexInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Custom Hex Color Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customColorHexInput,
                        onValueChange = { input ->
                            customColorHexInput = input
                            val cleanHex = if (input.startsWith("#")) input else "#$input"
                            if (cleanHex.length == 7 && cleanHex.matches(Regex("#[0-9a-fA-F]{6}"))) {
                                selectedThemeColor = cleanHex
                            }
                        },
                        label = { Text("Custom Color Hex (#RRGGBB)") },
                        placeholder = { Text("e.g. #FF5722 or #2E7D32") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(selectedThemeColor))
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                        }
                    )
                    if (customColorHexInput.isNotBlank()) {
                        Button(
                            onClick = {
                                val cleanHex = if (customColorHexInput.startsWith("#")) customColorHexInput else "#$customColorHexInput"
                                if (cleanHex.length == 7 && cleanHex.matches(Regex("#[0-9a-fA-F]{6}"))) {
                                    selectedThemeColor = cleanHex
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Organizer (Profile Owner) Configuration Section
                Text(
                    "Your Profile Role in this Tour",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Are you traveling solo or with your family?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!organizerIsFamily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { organizerIsFamily = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = !organizerIsFamily, onClick = { organizerIsFamily = false })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Solo Member\n(1.0x share)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!organizerIsFamily) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (organizerIsFamily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { organizerIsFamily = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = organizerIsFamily, onClick = { organizerIsFamily = true })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "My Family\n(Custom weight)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (organizerIsFamily) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (organizerIsFamily) {
                            OutlinedTextField(
                                value = organizerFamilyName,
                                onValueChange = { organizerFamilyName = it },
                                label = { Text("Family Name") },
                                placeholder = { Text("e.g. My Family") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            // Adults counter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Adults (1.0x share)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (organizerAdultsCount > 1) organizerAdultsCount-- },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("$organizerAdultsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                    IconButton(
                                        onClick = { organizerAdultsCount++ },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Kids counter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kids / Children (0.5x share)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (organizerKidsCount > 0) {
                                                organizerKidsCount--
                                                if (organizerKidAges.isNotEmpty()) organizerKidAges.removeAt(organizerKidAges.lastIndex)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("$organizerKidsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                    IconButton(
                                        onClick = {
                                            organizerKidsCount++
                                            organizerKidAges.add(5)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Kid age selectors
                            if (organizerKidsCount > 0) {
                                Text("Select Ages for Kids:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                for (k in 0 until organizerKidsCount) {
                                    val idx = k
                                    val currentAge = if (idx < organizerKidAges.size) organizerKidAges[idx] else 5
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Child ${idx + 1} Age:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    while (organizerKidAges.size <= idx) organizerKidAges.add(5)
                                                    if (organizerKidAges[idx] > 0) organizerKidAges[idx]--
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                            }
                                            Text("$currentAge yrs", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                            IconButton(
                                                onClick = {
                                                    while (organizerKidAges.size <= idx) organizerKidAges.add(5)
                                                    if (organizerKidAges[idx] < 17) organizerKidAges[idx]++
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Customizable Weight
                            val autoWeight = (organizerAdultsCount * 1.0f) + (organizerKidsCount * 0.5f)
                            OutlinedTextField(
                                value = organizerCustomWeight,
                                onValueChange = { organizerCustomWeight = it },
                                label = { Text("Custom Split Weight (Default: ${autoWeight}x)") },
                                placeholder = { Text("$autoWeight") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = organizerMembersInput,
                                onValueChange = { organizerMembersInput = it },
                                label = { Text("Family Member Names") },
                                placeholder = { Text("e.g. Myself, Spouse, Leo") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Companion Accounts Joining Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Companion Accounts Joining:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${membersList.size} Added",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Show added accounts list with expandable member breakdown
                if (membersList.isNotEmpty()) {
                    membersList.forEachIndexed { idx, pair ->
                        val isExpanded = viewingMemberDetailsIdx == idx
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewingMemberDetailsIdx = if (isExpanded) null else idx
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Group,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pair.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(pair.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(
                                        onClick = { membersList.removeAt(idx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Companion Member / Family Account Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Add Companion (Member or Family)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!companionIsFamily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { companionIsFamily = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = !companionIsFamily, onClick = { companionIsFamily = false })
                                    Text(
                                        "Solo Member",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!companionIsFamily) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (companionIsFamily) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { companionIsFamily = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = companionIsFamily, onClick = { companionIsFamily = true })
                                    Text(
                                        "Family Account",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (companionIsFamily) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = companionAccountName,
                            onValueChange = { companionAccountName = it },
                            label = { Text(if (companionIsFamily) "Family / Group Name" else "Member Name") },
                            placeholder = { Text(if (companionIsFamily) "e.g. The Coopers" else "e.g. Liam") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (companionIsFamily) {
                            // Adults & Kids steppers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Adults", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (companionAdultsCount > 1) companionAdultsCount-- },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("$companionAdultsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                    IconButton(
                                        onClick = { companionAdultsCount++ },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kids / Dependents", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (companionKidsCount > 0) {
                                                companionKidsCount--
                                                if (companionKidAges.isNotEmpty()) companionKidAges.removeAt(companionKidAges.lastIndex)
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Text("$companionKidsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
                                    IconButton(
                                        onClick = {
                                            companionKidsCount++
                                            companionKidAges.add(6)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Kid age selectors for companion family
                            if (companionKidsCount > 0) {
                                Text("Select Ages for Kids:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                for (k in 0 until companionKidsCount) {
                                    val idx = k
                                    val cAge = if (idx < companionKidAges.size) companionKidAges[idx] else 6
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Child ${idx + 1} Age:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    while (companionKidAges.size <= idx) companionKidAges.add(6)
                                                    if (companionKidAges[idx] > 0) companionKidAges[idx]--
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                            }
                                            Text("$cAge yrs", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                            IconButton(
                                                onClick = {
                                                    while (companionKidAges.size <= idx) companionKidAges.add(6)
                                                    if (companionKidAges[idx] < 17) companionKidAges[idx]++
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            val compAutoWeight = (companionAdultsCount * 1.0f) + (companionKidsCount * 0.5f)
                            OutlinedTextField(
                                value = companionCustomWeight,
                                onValueChange = { companionCustomWeight = it },
                                label = { Text("Custom Split Weight (Default: ${compAutoWeight}x)") },
                                placeholder = { Text("$compAutoWeight") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = companionMembersInput,
                            onValueChange = { companionMembersInput = it },
                            label = { Text("Member Names") },
                            placeholder = { Text(if (companionIsFamily) "e.g. Liam, Emma, Timmy" else "e.g. Liam") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (companionAccountName.isNotBlank()) {
                                    if (companionIsFamily) {
                                        val cAuto = (companionAdultsCount * 1.0f) + (companionKidsCount * 0.5f)
                                        val finalWeight = companionCustomWeight.toFloatOrNull() ?: cAuto
                                        val agesStr = if (companionKidsCount > 0) {
                                            val ages = companionKidAges.take(companionKidsCount).joinToString(", ") { "$it yrs" }
                                            ", Kids: $ages"
                                        } else ""
                                        val namesStr = if (companionMembersInput.isNotBlank()) " (${companionMembersInput.trim()})" else ""
                                        val note = "Family ($companionAdultsCount Adults$agesStr)$namesStr, weight:$finalWeight"
                                        membersList.add(companionAccountName.trim() to note)
                                    } else {
                                        val note = "Solo Member (${companionMembersInput.ifBlank { companionAccountName.trim() }}), weight:1.0"
                                        membersList.add(companionAccountName.trim() to note)
                                    }
                                    companionAccountName = ""
                                    companionMembersInput = ""
                                    companionCustomWeight = ""
                                    companionAdultsCount = 2
                                    companionKidsCount = 0
                                    companionKidAges.clear()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "+ Add Companion Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = budgetStr.toDoubleOrNull() ?: 1500.0
                    val now = System.currentTimeMillis()
                    val autoWeight = (organizerAdultsCount * 1.0f) + (organizerKidsCount * 0.5f)
                    val finalOrgWeight = if (organizerIsFamily) {
                        organizerCustomWeight.toFloatOrNull() ?: autoWeight
                    } else 1.0f

                    val orgKidsInfo = if (organizerIsFamily && organizerKidsCount > 0) {
                        val ages = organizerKidAges.take(organizerKidsCount).joinToString(", ") { "$it yrs" }
                        "$organizerAdultsCount Adults, $organizerKidsCount Kids ($ages)"
                    } else if (organizerIsFamily) {
                        "$organizerAdultsCount Adults"
                    } else ""

                    onConfirm(
                        title,
                        destination,
                        now,
                        now + 86400000L * 5,
                        currencySymbol,
                        budget,
                        selectedThemeColor,
                        membersList,
                        organizerIsFamily,
                        finalOrgWeight,
                        organizerFamilyName,
                        organizerMembersInput,
                        orgKidsInfo
                    )
                },
                modifier = Modifier.testTag("confirm_create_tour_btn")
            ) {
                Text("Create Tour")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    members: List<MemberEntity>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onQuickAddMember: ((name: String) -> Unit)? = null,
    onOpenAddMember: (() -> Unit)? = null,
    onConfirm: (
        title: String,
        amount: Double,
        category: String,
        paidById: String,
        splitType: String,
        splitMemberIds: List<String>,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var paidById by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
    var splitType by remember { mutableStateOf(SplitType.EQUAL) }
    var notes by remember { mutableStateOf("") }
    var quickMemberName by remember { mutableStateOf("") }

    val selectedSplitMemberIds = remember {
        mutableStateListOf<String>().apply {
            addAll(members.map { it.id })
        }
    }

    // Keep member selections and payer in sync whenever members updates
    LaunchedEffect(members) {
        if (members.isNotEmpty()) {
            if (paidById.isBlank() || members.none { it.id == paidById }) {
                paidById = members.first().id
            }
            if (selectedSplitMemberIds.isEmpty()) {
                selectedSplitMemberIds.addAll(members.map { it.id })
            } else {
                members.forEach { m ->
                    if (!selectedSplitMemberIds.contains(m.id)) {
                        selectedSplitMemberIds.add(m.id)
                    }
                }
            }
        }
    }

    // Custom exact amount inputs for each member when CUSTOM split is selected
    val customAmounts = remember { androidx.compose.runtime.mutableStateMapOf<String, String>() }
    var payerExpanded by remember { mutableStateOf(false) }
    val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Compute live shares
    val memberMap = remember(members) { members.associateBy { it.id } }
    val selectedMembers = members.filter { selectedSplitMemberIds.contains(it.id) }
    val totalWeight = selectedMembers.sumOf { it.weight.toDouble() }.let { if (it > 0.0) it else 1.0 }

    // If Custom is selected and user changes amount or enters custom mode, initialize blank amounts evenly
    LaunchedEffect(parsedAmount, splitType, selectedSplitMemberIds.size) {
        if (splitType == SplitType.CUSTOM && parsedAmount > 0.0 && selectedMembers.isNotEmpty()) {
            val allBlankOrZero = selectedMembers.all { (customAmounts[it.id]?.toDoubleOrNull() ?: 0.0) == 0.0 }
            if (allBlankOrZero) {
                val even = String.format(Locale.getDefault(), "%.2f", parsedAmount / selectedMembers.size)
                selectedMembers.forEach { m ->
                    customAmounts[m.id] = even
                }
            }
        }
    }

    // Custom total allocated
    val customTotalAllocated = selectedMembers.sumOf { m ->
        customAmounts[m.id]?.toDoubleOrNull() ?: 0.0
    }
    val customRemaining = parsedAmount - customTotalAllocated

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // FIXED HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = parseHexColor(selectedCategory.colorHex).copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getCategoryIcon(selectedCategory),
                                    contentDescription = null,
                                    tint = parseHexColor(selectedCategory.colorHex),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "Add Tour Expense",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (parsedAmount > 0.0) "Total: $currencySymbol${String.format(Locale.getDefault(), "%.2f", parsedAmount)} • ${selectedSplitMemberIds.size} sharing"
                                else "Record expense and split with members",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close dialog")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // SCROLLABLE BODY
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent()
                                if (scrollState.maxValue > 0) {
                                    val viewHeight = size.height
                                    val totalScrollRange = (scrollState.maxValue + viewHeight).toFloat()
                                    val thumbHeight = ((viewHeight / totalScrollRange) * viewHeight).coerceIn(40f, viewHeight * 0.7f)
                                    val scrollOffsetFraction = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                                    val thumbTop = scrollOffsetFraction * (viewHeight - thumbHeight)

                                    drawRoundRect(
                                        color = Color.LightGray.copy(alpha = 0.25f),
                                        topLeft = Offset(size.width - 7.dp.toPx(), 4.dp.toPx()),
                                        size = Size(5.dp.toPx(), viewHeight - 8.dp.toPx()),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    drawRoundRect(
                                        color = Color(0xFF6750A4).copy(alpha = 0.75f),
                                        topLeft = Offset(size.width - 7.dp.toPx(), thumbTop),
                                        size = Size(5.dp.toPx(), thumbHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("What was this for?") },
                            placeholder = { Text("e.g. Seafood Lunch, Fuel, Museum") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_title_input"),
                            singleLine = true
                        )

                        // Amount
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text("Total Amount ($currencySymbol)") },
                            prefix = { Text(currencySymbol, fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_amount_input"),
                            singleLine = true
                        )

                        // Category
                        Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExpenseCategory.entries.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                val catColor = parseHexColor(cat.colorHex)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) catColor else catColor.copy(alpha = 0.12f),
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else catColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = cat.displayName,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        // EMPTY STATE FOR 0 MEMBERS
                        if (members.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "No Members in this Tour Yet",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "To record who paid and split the cost, add at least one member to this tour:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = quickMemberName,
                                            onValueChange = { quickMemberName = it },
                                            label = { Text("Member Name") },
                                            placeholder = { Text("e.g. Myself, Alice") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                if (quickMemberName.isNotBlank()) {
                                                    onQuickAddMember?.invoke(quickMemberName.trim())
                                                    quickMemberName = ""
                                                }
                                            },
                                            enabled = quickMemberName.isNotBlank(),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("+ Add")
                                        }
                                    }
                                    if (onOpenAddMember != null) {
                                        TextButton(
                                            onClick = onOpenAddMember,
                                            modifier = Modifier.align(Alignment.End),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("+ Add Detailed Family Account", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // Who Paid Upfront
                        Text("Who Paid Upfront?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        if (members.isNotEmpty()) {
                            ExposedDropdownMenuBox(
                                expanded = payerExpanded,
                                onExpandedChange = { payerExpanded = !payerExpanded }
                            ) {
                                val payerMember = members.firstOrNull { it.id == paidById } ?: members.firstOrNull()
                                OutlinedTextField(
                                    value = payerMember?.let { "${it.name} (${it.familyNote})" } ?: "Select Member",
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = {
                                        if (payerMember != null) {
                                            MemberAvatar(name = payerMember.name, avatarColorHex = payerMember.avatarColorHex, size = 24.dp)
                                        }
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = payerExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = payerExpanded,
                                    onDismissRequest = { payerExpanded = false }
                                ) {
                                    members.forEach { m ->
                                        DropdownMenuItem(
                                            leadingIcon = {
                                                MemberAvatar(name = m.name, avatarColorHex = m.avatarColorHex, size = 26.dp)
                                            },
                                            text = {
                                                Column {
                                                    Text(m.name, fontWeight = FontWeight.SemiBold)
                                                    Text(m.familyNote, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                paidById = m.id
                                                payerExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = "Please add a tour member above first",
                                onValueChange = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // HOW TO SPLIT SECTION
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("How to Split?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            if (members.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.clickable {
                                        coroutineScope.launch {
                                            scrollState.animateScrollTo(scrollState.maxValue)
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Jump to Members", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                        Icon(
                                            Icons.Default.KeyboardDoubleArrowDown,
                                            contentDescription = "Scroll down",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Segmented Control for Split Type
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val splitOptions = listOf(
                                Triple(SplitType.EQUAL, "⚡ Equal", "Split evenly"),
                                Triple(SplitType.BY_WEIGHT, "👨‍👩‍👧 By Family", "By head count"),
                                Triple(SplitType.CUSTOM, "✍️ Custom", "Exact owes")
                            )

                            splitOptions.forEach { (type, titleStr, subtitleStr) ->
                                val isSelected = splitType == type
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            splitType = type
                                            if (type == SplitType.CUSTOM) {
                                                if (parsedAmount > 0.0 && selectedMembers.isNotEmpty()) {
                                                    val even = String.format(Locale.getDefault(), "%.2f", parsedAmount / selectedMembers.size)
                                                    selectedMembers.forEach { m ->
                                                        if (customAmounts[m.id].isNullOrBlank() || (customAmounts[m.id]?.toDoubleOrNull() ?: 0.0) == 0.0) {
                                                            customAmounts[m.id] = even
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = titleStr,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = subtitleStr,
                                            fontSize = 9.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Clear Explanation Card for Active Split
                        when (splitType) {
                            SplitType.EQUAL -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            val perPerson = if (selectedSplitMemberIds.isNotEmpty()) parsedAmount / selectedSplitMemberIds.size else 0.0
                                            Text(
                                                "⚡ Equal Split Active",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                if (parsedAmount > 0.0) "Total $currencySymbol${String.format(Locale.getDefault(), "%.2f", parsedAmount)} split among ${selectedSplitMemberIds.size} members = $currencySymbol${String.format(Locale.getDefault(), "%.2f", perPerson)} each."
                                                else "Enter amount above to see each member's equal share.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            SplitType.BY_WEIGHT -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Group,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                "👨‍👩‍👧 Family Weight Split Active",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                            val ratePerUnit = if (totalWeight > 0.0) parsedAmount / totalWeight else 0.0
                                            Text(
                                                "Total headcount: ${String.format(Locale.getDefault(), "%.1f", totalWeight)}x units • $currencySymbol${String.format(Locale.getDefault(), "%.2f", ratePerUnit)} per 1.0x unit. Larger families pay proportionally more.",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            SplitType.CUSTOM -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "✍️ Custom Allocation Controls",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            val isBalanced = kotlin.math.abs(customRemaining) < 0.05
                                            Surface(
                                                color = if (isBalanced) Color(0xFFE8F5E9) else if (customRemaining > 0) Color(0xFFFFF3E0) else Color(0xFFFFEBEE),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    if (isBalanced) "✅ Balanced"
                                                    else if (customRemaining > 0) "Remains: $currencySymbol${String.format(Locale.getDefault(), "%.2f", customRemaining)}"
                                                    else "Over: $currencySymbol${String.format(Locale.getDefault(), "%.2f", -customRemaining)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isBalanced) Color(0xFF2E7D32) else if (customRemaining > 0) Color(0xFFE65100) else Color(0xFFC62828),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            "Allocated: $currencySymbol${String.format(Locale.getDefault(), "%.2f", customTotalAllocated)} of $currencySymbol${String.format(Locale.getDefault(), "%.2f", parsedAmount)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (parsedAmount > 0.0 && selectedSplitMemberIds.isNotEmpty()) {
                                                        val even = String.format(Locale.getDefault(), "%.2f", parsedAmount / selectedSplitMemberIds.size)
                                                        selectedSplitMemberIds.forEach { mId ->
                                                            customAmounts[mId] = even
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("⚡ Auto-Even", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    if (customRemaining > 0.01) {
                                                        val targetId = selectedSplitMemberIds.firstOrNull {
                                                            (customAmounts[it]?.toDoubleOrNull() ?: 0.0) == 0.0
                                                        } ?: selectedSplitMemberIds.firstOrNull()
                                                        if (targetId != null) {
                                                            val currentVal = customAmounts[targetId]?.toDoubleOrNull() ?: 0.0
                                                            val newVal = currentVal + customRemaining
                                                            customAmounts[targetId] = String.format(Locale.getDefault(), "%.2f", newVal)
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("🎯 Fill Left", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    selectedSplitMemberIds.forEach { mId ->
                                                        customAmounts[mId] = "0.00"
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Clear", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // MEMBERS INCLUDED SECTION
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Robust header with no text-wrapping bug
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Split with (${selectedSplitMemberIds.size}/${members.size} members)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (members.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.clickable {
                                                    selectedSplitMemberIds.clear()
                                                    selectedSplitMemberIds.addAll(members.map { it.id })
                                                }
                                            ) {
                                                Text(
                                                    "Select All",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.clickable {
                                                    if (selectedSplitMemberIds.size > 1) {
                                                        val first = selectedSplitMemberIds.first()
                                                        selectedSplitMemberIds.clear()
                                                        selectedSplitMemberIds.add(first)
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    "Reset",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (members.isEmpty()) {
                                    Text(
                                        "No members to display yet. Add a member above.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                members.forEach { m ->
                                    val isChecked = selectedSplitMemberIds.contains(m.id)
                                    val perPersonEqual = if (selectedSplitMemberIds.isNotEmpty()) parsedAmount / selectedSplitMemberIds.size else 0.0
                                    val perPersonWeight = if (totalWeight > 0.0) (parsedAmount * m.weight) / totalWeight else 0.0

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isChecked) MaterialTheme.colorScheme.surface
                                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isChecked) {
                                                        if (selectedSplitMemberIds.size > 1) selectedSplitMemberIds.remove(m.id)
                                                    } else {
                                                        selectedSplitMemberIds.add(m.id)
                                                    }
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { check ->
                                                    if (check) selectedSplitMemberIds.add(m.id)
                                                    else if (selectedSplitMemberIds.size > 1) selectedSplitMemberIds.remove(m.id)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            MemberAvatar(name = m.name, avatarColorHex = m.avatarColorHex, size = 30.dp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    m.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    if (m.familyMembersText.isNotBlank()) "${m.familyNote} (${m.familyMembersText})" else m.familyNote,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                                if (splitType == SplitType.BY_WEIGHT) {
                                                    Text(
                                                        "Family Weight: ${m.weight}x",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.tertiary,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            // Calculated share
                                            if (isChecked) {
                                                when (splitType) {
                                                    SplitType.EQUAL -> {
                                                        Text(
                                                            "$currencySymbol${String.format(Locale.getDefault(), "%.2f", perPersonEqual)}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    SplitType.BY_WEIGHT -> {
                                                        Text(
                                                            "$currencySymbol${String.format(Locale.getDefault(), "%.2f", perPersonWeight)}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = MaterialTheme.colorScheme.tertiary
                                                        )
                                                    }
                                                    SplitType.CUSTOM -> {
                                                        val userEntered = customAmounts[m.id]?.toDoubleOrNull() ?: 0.0
                                                        Text(
                                                            "$currencySymbol${String.format(Locale.getDefault(), "%.2f", userEntered)}",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    "$currencySymbol 0.00",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                            }
                                        }

                                        // Custom exact amount input if Custom split is active and member is selected
                                        if (isChecked && splitType == SplitType.CUSTOM) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 38.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text("Owes:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                                    OutlinedTextField(
                                                        value = customAmounts[m.id] ?: "",
                                                        onValueChange = { customAmounts[m.id] = it },
                                                        placeholder = { Text("0.00") },
                                                        prefix = { Text(currencySymbol, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                        modifier = Modifier.weight(1f),
                                                        singleLine = true
                                                    )
                                                }

                                                // Quick increment shortcut chips
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    listOf(5.0, 10.0, 20.0).forEach { inc ->
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                                            modifier = Modifier.clickable {
                                                                val curr = customAmounts[m.id]?.toDoubleOrNull() ?: 0.0
                                                                customAmounts[m.id] = String.format(Locale.getDefault(), "%.2f", curr + inc)
                                                            }
                                                        ) {
                                                            Text(
                                                                "+$currencySymbol${inc.toInt()}",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }

                                                    if (customRemaining > 0.01) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color(0xFFE8F5E9),
                                                            modifier = Modifier.clickable {
                                                                val curr = customAmounts[m.id]?.toDoubleOrNull() ?: 0.0
                                                                customAmounts[m.id] = String.format(Locale.getDefault(), "%.2f", curr + customRemaining)
                                                            }
                                                        ) {
                                                            Text(
                                                                "+All Left ($currencySymbol${String.format(Locale.getDefault(), "%.0f", customRemaining)})",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF2E7D32),
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }

                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier.clickable {
                                                            customAmounts[m.id] = "0.00"
                                                        }
                                                    ) {
                                                        Text(
                                                            "0.00",
                                                            fontSize = 10.sp,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Optional Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Bill Memo (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Floating scroll cue button
                    if (scrollState.canScrollForward) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .clickable {
                                    coroutineScope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Scroll to member owes",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = "Scroll down",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // FIXED STICKY FOOTER
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                if (title.isNotBlank() && amount > 0.0 && paidById.isNotBlank()) {
                                    val finalSplitMemberIds = if (splitType == SplitType.CUSTOM) {
                                        selectedSplitMemberIds.map { mId ->
                                            val customVal = customAmounts[mId]?.toDoubleOrNull() ?: (amount / selectedSplitMemberIds.size)
                                            "$mId:$customVal"
                                        }
                                    } else {
                                        selectedSplitMemberIds.toList()
                                    }

                                    onConfirm(
                                        title,
                                        amount,
                                        selectedCategory.id,
                                        paidById,
                                        splitType.id,
                                        finalSplitMemberIds,
                                        notes
                                    )
                                }
                            },
                            enabled = title.isNotBlank() && parsedAmount > 0.0 && paidById.isNotBlank() && selectedSplitMemberIds.isNotEmpty(),
                            modifier = Modifier.testTag("confirm_add_expense_btn")
                        ) {
                            Text(
                                if (parsedAmount > 0.0) "Save Expense ($currencySymbol${String.format(Locale.getDefault(), "%.2f", parsedAmount)})"
                                else "Save Expense"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddFamilyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        familyNote: String,
        weight: Float,
        colorHex: String,
        familyMembersText: String
    ) -> Unit
) {
    var familyName by remember { mutableStateOf("") }
    var adultsCount by remember { mutableStateOf(2) }
    var kidsCount by remember { mutableStateOf(1) }
    var kidAges by remember { mutableStateOf(listOf(6)) }
    var membersListStr by remember { mutableStateOf("") }
    var isCustomWeight by remember { mutableStateOf(false) }
    var customWeightStr by remember { mutableStateOf("2.5") }
    var selectedColor by remember { mutableStateOf("#E67E22") }

    val computedAutoWeight = (adultsCount * 1.0f) + (kidsCount * 0.5f)
    val effectiveWeight = if (isCustomWeight) {
        customWeightStr.trim()
            .replace("x", "", ignoreCase = true)
            .replace("X", "")
            .replace(",", ".")
            .replace(" ", "")
            .toFloatOrNull() ?: computedAutoWeight
    } else {
        computedAutoWeight
    }

    val noteLabel = when {
        adultsCount == 1 && kidsCount == 0 -> "Solo (1 Adult)"
        adultsCount == 2 && kidsCount == 0 -> "Couple (2 Adults)"
        kidsCount > 0 -> {
            val agesSummary = kidAges.take(kidsCount).joinToString(", ") { "$it y/o" }
            "Family ($adultsCount Adults, $kidsCount ${if (kidsCount == 1) "Child" else "Children"} [Ages: $agesSummary])"
        }
        else -> "$adultsCount Adults"
    }

    val colors = listOf("#0D5C75", "#E67E22", "#9C27B0", "#2E7D32", "#00838F", "#D81B60", "#F57C00", "#5C6BC0")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Family Account", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✨ One Account, Unlimited Family Members! All members of this family will be grouped into 1 account for debt settlement.",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Family / Account Name") },
                    placeholder = { Text("e.g. The Miller Family or Alex & Mia") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input"),
                    singleLine = true
                )

                // Steppers for Adults and Children & Custom Weight
                Text("Family Composition & Split Weight", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Adults row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Adults", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("1.0x share weight each", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (adultsCount > 1) adultsCount-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease adults")
                                }
                                Text("$adultsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = { adultsCount++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase adults")
                                }
                            }
                        }

                        Divider()

                        // Children row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Children / Kids", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("0.5x half-share weight each", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (kidsCount > 0) {
                                            kidsCount--
                                            if (kidAges.size > kidsCount) {
                                                kidAges = kidAges.take(kidsCount)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease kids")
                                }
                                Text("$kidsCount", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(
                                    onClick = {
                                        kidsCount++
                                        kidAges = kidAges + listOf(5)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase kids")
                                }
                            }
                        }

                        // Kids age selector
                        if (kidsCount > 0) {
                            Divider()
                            Text(
                                "Specify Kid Ages:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            for (i in 0 until kidsCount) {
                                val currentAge = kidAges.getOrElse(i) { 5 }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Child ${i + 1} Age", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (currentAge > 0) {
                                                    val newAges = kidAges.toMutableList()
                                                    if (i < newAges.size) newAges[i] = currentAge - 1
                                                    kidAges = newAges
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease age", modifier = Modifier.size(16.dp))
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Text(
                                                if (currentAge == 0) "<1 yr" else "$currentAge yrs",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (currentAge < 17) {
                                                    val newAges = kidAges.toMutableList()
                                                    if (i < newAges.size) newAges[i] = currentAge + 1
                                                    kidAges = newAges
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase age", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Divider()

                        // Custom split weight option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Custom Split Weight", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Override standard calculated share", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            androidx.compose.material3.Switch(
                                checked = isCustomWeight,
                                onCheckedChange = {
                                    isCustomWeight = it
                                    if (it) {
                                        customWeightStr = String.format(Locale.getDefault(), "%.1f", computedAutoWeight)
                                    }
                                }
                            )
                        }

                        if (isCustomWeight) {
                            OutlinedTextField(
                                value = customWeightStr,
                                onValueChange = { customWeightStr = it },
                                label = { Text("Custom Weight Value (e.g. 2.25)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Total Fair Share Weight: ${String.format(Locale.getDefault(), "%.2f", effectiveWeight)}x ($noteLabel)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Family Member Names
                OutlinedTextField(
                    value = membersListStr,
                    onValueChange = { membersListStr = it },
                    label = { Text("Family Member Names") },
                    placeholder = { Text("e.g. Liam, Emma, Noah, Olivia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Avatar Color", style = MaterialTheme.typography.labelMedium)
                    Text("Select color", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colors.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (familyName.isNotBlank()) {
                        val namesText = membersListStr.ifBlank { familyName }
                        onConfirm(
                            familyName.trim(),
                            noteLabel,
                            effectiveWeight,
                            selectedColor,
                            namesText
                        )
                    }
                },
                modifier = Modifier.testTag("confirm_add_member_btn")
            ) {
                Text("Add Family Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, familyNote: String, weight: Float, colorHex: String) -> Unit
) {
    AddFamilyDialog(
        onDismiss = onDismiss,
        onConfirm = { name, note, weight, color, _ ->
            onConfirm(name, note, weight, color)
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageFamilyMembersDialog(
    member: MemberEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, note: String, weight: Float, membersText: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var familyName by remember(member.id) { mutableStateOf(member.name) }
    var membersText by remember(member.id) { mutableStateOf(member.familyMembersText.ifBlank { member.name }) }
    var familyNote by remember(member.id) { mutableStateOf(member.familyNote) }
    var weightVal by remember(member.id, member.weight) { mutableFloatStateOf(member.weight) }
    var weightStr by remember(member.id, member.weight) {
        val formatted = if (member.weight % 1.0f == 0.0f) {
            member.weight.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", member.weight).trimEnd('0').trimEnd('.')
        }
        mutableStateOf(formatted)
    }

    fun parseCleanWeight(text: String, fallback: Float): Float {
        val sanitized = text.trim()
            .replace("x", "", ignoreCase = true)
            .replace("X", "")
            .replace(",", ".")
            .replace(" ", "")
        val parsed = sanitized.toFloatOrNull()
        return if (parsed != null && parsed > 0f) parsed else fallback
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(name = member.name, avatarColorHex = member.avatarColorHex, size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Account", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                val isOrganizer = member.familyNote.contains("Organizer", ignoreCase = true)
                if (onDelete != null && !isOrganizer) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Account",
                            tint = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✨ All member names and details are permanently saved once updated. Accounts can only be deleted by the profile owner.",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value = familyName,
                    onValueChange = { 
                        familyName = it 
                        if (membersText == "Tour Member" || membersText == member.name) {
                            membersText = it
                        }
                    },
                    label = { Text("Member / Family Account Name") },
                    placeholder = { Text("e.g. Liam, Emma, The Coopers") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = membersText,
                    onValueChange = { membersText = it },
                    label = { Text("Individual Members in this Account") },
                    placeholder = { Text("e.g. Liam, Emma (all people in this account)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = familyNote,
                    onValueChange = { familyNote = it },
                    label = { Text("Composition Tag") },
                    placeholder = { Text("e.g. Family (2 Adults, 1 Kid)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Split Weight Section with Steppers & Quick Chips
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Account Split Weight",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Controls how expenses are split. A 2.0x weight pays twice as much as a 1.0x solo member.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Stepper + input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    val cur = parseCleanWeight(weightStr, weightVal)
                                    val next = (cur - 0.5f).coerceAtLeast(0.5f)
                                    weightVal = next
                                    weightStr = if (next % 1.0f == 0.0f) next.toInt().toString() else String.format(Locale.US, "%.1f", next)
                                },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease weight")
                            }

                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = {
                                    weightStr = it
                                    val p = parseCleanWeight(it, -1f)
                                    if (p > 0f) weightVal = p
                                },
                                label = { Text("Split Weight") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            FilledTonalIconButton(
                                onClick = {
                                    val cur = parseCleanWeight(weightStr, weightVal)
                                    val next = cur + 0.5f
                                    weightVal = next
                                    weightStr = if (next % 1.0f == 0.0f) next.toInt().toString() else String.format(Locale.US, "%.1f", next)
                                },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase weight")
                            }
                        }

                        // Quick Preset Chips
                        Text("Quick Weight Presets:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                0.5f to "0.5x Child",
                                1.0f to "1.0x Solo",
                                1.5f to "1.5x",
                                2.0f to "2.0x Couple",
                                2.5f to "2.5x",
                                3.0f to "3.0x Family",
                                3.5f to "3.5x",
                                4.0f to "4.0x"
                            ).forEach { (presetW, label) ->
                                val isSelected = Math.abs(weightVal - presetW) < 0.05f
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.clickable {
                                        weightVal = presetW
                                        weightStr = if (presetW % 1.0f == 0.0f) presetW.toInt().toString() else String.format(Locale.US, "%.1f", presetW)
                                    }
                                ) {
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeW = parseCleanWeight(weightStr, weightVal)
                            val displayW = if (activeW % 1.0f == 0.0f) activeW.toInt().toString() else String.format(Locale.US, "%.2f", activeW).trimEnd('0').trimEnd('.')
                            Text(
                                "⚖️ Saving with ${displayW}x split weight",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = familyName.trim().ifBlank { member.name }
                    val cleanMembers = membersText.trim().let {
                        if (it.isBlank() || it == "Tour Member") cleanName else it
                    }
                    val cleanNote = familyNote.trim().ifBlank { member.familyNote }
                    val finalWeight = parseCleanWeight(weightStr, weightVal).coerceAtLeast(0.1f)
                    onSave(cleanName, cleanNote, finalWeight, cleanMembers)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RecordSettlementDialog(
    fromName: String,
    toName: String,
    suggestedAmount: Double,
    currencySymbol: String,
    tourTitle: String = "",
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(String.format(Locale.getDefault(), "%.2f", suggestedAmount)) }
    var note by remember { mutableStateOf("Direct settlement payment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settle Debt with $toName", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "$fromName is paying $toName $currencySymbol$amountStr to balance the tour expenses.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Settlement Amount ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Payment Memo / Confirmation Note") },
                    placeholder = { Text("e.g. Paid via Cash, Bank Transfer, or Direct") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: suggestedAmount
                    if (amt > 0.0) {
                        onConfirm(amt, note)
                    }
                }
            ) {
                Text("Confirm as Paid")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViewTourMembersDialog(
    tourTitle: String,
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onManageMember: (MemberEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Tour Members & Families", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(tourTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✨ 1 Account Rule: Each card below is 1 account for debt settlements, containing up to 10 individual family members.",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                members.forEach { m ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onManageMember(m)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemberAvatar(name = m.name, avatarColorHex = m.avatarColorHex, size = 36.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(m.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        "${m.familyNote} • Split Weight: ${m.weight}x",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        onManageMember(m)
                                        onDismiss()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Edit", fontSize = 11.sp)
                                }
                            }

                            // Individual Member Names
                            val namesList = if (m.familyMembersText.isNotBlank()) {
                                m.familyMembersText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            } else {
                                listOf(m.name)
                            }

                            Text(
                                "Who is in this family (${namesList.size}):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                namesList.forEach { pName ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(pName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
