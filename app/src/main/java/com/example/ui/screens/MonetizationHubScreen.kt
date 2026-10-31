package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TourViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetizationHubScreen(
    viewModel: TourViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isProUnlocked by viewModel.isProUnlocked.collectAsState()

    var activeGroupsSlider by remember { mutableFloatStateOf(500f) }
    var conversionRateSlider by remember { mutableFloatStateOf(5f) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Owner Monetization Suite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("monetization_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "App Monetization Blueprint",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    "How to generate high recurring revenue from YIGRO",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Group travel apps have some of the highest user intent and transactional spend in the app industry. Friends and families on vacation routinely spend $1,000 to $10,000+ per tour. Here are 5 practical, proven monetization models designed for your app.",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Interactive Live Pro Mode Tester for Owner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Diamond, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Test YIGRO Pro In-App",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                "Toggle this to preview the unlocked Pro experience (PDF reports, unlimited tours, badge)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isProUnlocked,
                            onCheckedChange = {
                                viewModel.toggleProUnlocked()
                                Toast.makeText(
                                    context,
                                    if (!isProUnlocked) "YIGRO Pro Unlocked! Check out Tour options." else "Pro mode disabled.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("toggle_pro_switch")
                        )
                    }
                }
            }

            // Strategy 1: Freemium / Tour FD Pro
            item {
                RevenueStreamCard(
                    title = "1. YIGRO Subscriptions (1 Month & Yearly)",
                    tag = "Core Gatekeeper Model",
                    icon = Icons.Default.Diamond,
                    accentColor = Color(0xFF673AB7),
                    description = "Free tier is limited to 2 members per tour. For groups of 3 or more friends & family, a subscription is mandatory.",
                    bulletPoints = listOf(
                        "1-Month Subscription: $3.99 / month (Ideal for occasional family vacations)",
                        "1-Year Subscription: $24.99 / year (Save 48% • $2.08/mo for regular travelers)",
                        "Unlocks unlimited members, friends & family units per tour",
                        "Includes PDF and Excel formatted financial report exports",
                        "Enables receipt attachments and multi-payer split rules"
                    )
                )
            }

            // Strategy 2: Travel Affiliates
            item {
                RevenueStreamCard(
                    title = "2. Travel Affiliate Commissions (3% - 10%)",
                    tag = "Highest Payout Per User",
                    icon = Icons.Default.Hotel,
                    accentColor = Color(0xFF00897B),
                    description = "When a group creates a tour destination (e.g. 'Lake Como', 'Goa', 'Orlando'), integrate native affiliate links for group lodging, rental vans, and activities.",
                    bulletPoints = listOf(
                        "Booking.com / Airbnb Affiliate: 4% - 6% commission on hotel & villa stays",
                        "Viator / GetYourGuide: 8% - 10% commission on group tickets and boat tours",
                        "Rentalcars.com / Cab APIs: $15 - $30 bounty per rental van booking",
                        "Math: 1 group booking a $3,000 resort earns you $150 cash from just ONE booking!"
                    )
                )
            }

            // Strategy 3: Fintech & Payment Rails
            item {
                RevenueStreamCard(
                    title = "3. Settle-Up Fintech Integrations & Bounties",
                    tag = "Micro-fees & Referral Bounties",
                    icon = Icons.Default.Payments,
                    accentColor = Color(0xFF1E88E5),
                    description = "When friends settle debts, provide deep links to popular digital wallets (Venmo, PayPal, CashApp, Revolut, UPI).",
                    bulletPoints = listOf(
                        "New user signup referral bounties: $5 to $20 per user joining Revolut/CashApp via your link",
                        "Optional in-app instant settlement escrow fee: 0.5% micro-convenience fee",
                        "High conversion because settlement is the climax of every tour"
                    )
                )
            }

            // Strategy 4: Sponsored Local Deals
            item {
                RevenueStreamCard(
                    title = "4. Sponsored Local Dining & Activity Vouchers",
                    tag = "B2B Merchant Partnerships",
                    icon = Icons.Default.Restaurant,
                    accentColor = Color(0xFFE65100),
                    description = "Connect travel groups directly with local restaurants and attractions seeking high-spend family tables.",
                    bulletPoints = listOf(
                        "Group dining promotions: 'Show YIGRO for 15% off tables of 5+'",
                        "Restaurants pay $50 - $200/month for promoted placement in popular vacation cities",
                        "Win-win: Tourists save money, merchants get large groups, you earn sponsorship fees"
                    )
                )
            }

            // Strategy 5: Native Ads & Rewarded Exports
            item {
                RevenueStreamCard(
                    title = "5. Rewarded Video Ads for Free Exports",
                    tag = "Passive Ad Revenue",
                    icon = Icons.Default.PictureAsPdf,
                    accentColor = Color(0xFF0288D1),
                    description = "Let free users unlock premium features without paying cash by watching a short 15-second sponsor video.",
                    bulletPoints = listOf(
                        "Rewarded Ads (Google AdMob / Unity): $20 - $40 eCPM",
                        "Free users watch a video to export the WhatsApp summary or PDF report",
                        "Subtle banner ads for budget airline deals & luggage gear"
                    )
                )
            }

            // Interactive Revenue Calculator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Interactive Owner Revenue Calculator",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Slider 1: Active Monthly Groups
                        Text(
                            "Monthly Active Tour Groups: ${activeGroupsSlider.toInt()}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Slider(
                            value = activeGroupsSlider,
                            onValueChange = { activeGroupsSlider = it },
                            valueRange = 100f..5000f,
                            steps = 48
                        )

                        // Slider 2: Conversion to Pro / Affiliates
                        Text(
                            "Pro & Affiliate Conversion Rate: ${String.format(Locale.getDefault(), "%.1f", conversionRateSlider)}%",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                        Slider(
                            value = conversionRateSlider,
                            onValueChange = { conversionRateSlider = it },
                            valueRange = 1f..15f,
                            steps = 28
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic calculation
                        // Assume average group size: 5 people
                        // Pro subscription / pass: $4.99
                        // Affiliate booking per converted group: $75 average
                        val convertedGroups = (activeGroupsSlider * (conversionRateSlider / 100f))
                        val proEarnings = convertedGroups * 4.99
                        val affiliateEarnings = (convertedGroups * 0.4) * 85.0 // 40% of converted groups also book stay/activities
                        val monthlyTotal = proEarnings + affiliateEarnings
                        val annualTotal = monthlyTotal * 12

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Projected Owner Earnings:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            "$${String.format(Locale.getDefault(), "%,.0f", monthlyTotal)} / mo",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Monthly Recurring", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "$${String.format(Locale.getDefault(), "%,.0f", annualTotal)} / yr",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text("Annual Projected", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun RevenueStreamCard(
    title: String,
    tag: String,
    icon: ImageVector,
    accentColor: Color,
    description: String,
    bulletPoints: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        tag,
                        color = accentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                bulletPoints.forEach { pt ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            pt,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
