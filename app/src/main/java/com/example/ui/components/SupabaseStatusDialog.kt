package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.SupabaseConfig
import com.example.ui.viewmodel.TourViewModel

const val SUPABASE_SQL_SCHEMA = """-- Supabase SQL Schema for YIGRO
-- Run this in your Supabase SQL Editor:

create table if not exists public.profiles (
    id text primary key,
    login_id text not null,
    name text not null,
    email text not null,
    created_at bigint default (extract(epoch from now()) * 1000)
);

create table if not exists public.tours (
    id text primary key,
    user_id text,
    title text not null,
    destination text not null,
    start_date bigint,
    end_date bigint,
    currency_symbol text default '$',
    budget float8 default 0.0,
    theme_color_hex text default '#0D5C75',
    is_settled boolean default false,
    created_at bigint default (extract(epoch from now()) * 1000)
);

create table if not exists public.members (
    id text primary key,
    tour_id text not null,
    name text not null,
    family_note text default 'Solo',
    weight float4 default 1.0,
    avatar_color_hex text default '#0D5C75',
    family_members_text text default '',
    payment_upi_id text default '',
    payment_link text default ''
);

-- Migrations in case members table already exists
alter table public.members add column if not exists family_members_text text default '';
alter table public.members add column if not exists payment_upi_id text default '';
alter table public.members add column if not exists payment_link text default '';
alter table public.tours add column if not exists user_id text;

create table if not exists public.expenses (
    id text primary key,
    tour_id text not null,
    title text not null,
    amount float8 not null,
    date bigint,
    category text default 'OTHER',
    paid_by_id text not null,
    split_type text default 'EQUAL',
    split_member_ids text,
    notes text
);

create table if not exists public.settlement_payments (
    id text primary key,
    tour_id text not null,
    from_member_id text not null,
    to_member_id text not null,
    amount float8 not null,
    date bigint,
    notes text
);

-- Enable RLS and public access policies
alter table public.profiles enable row level security;
alter table public.tours enable row level security;
alter table public.members enable row level security;
alter table public.expenses enable row level security;
alter table public.settlement_payments enable row level security;

drop policy if exists "Public access to profiles" on public.profiles;
create policy "Public access to profiles" on public.profiles for all using (true) with check (true);

drop policy if exists "Public access to tours" on public.tours;
create policy "Public access to tours" on public.tours for all using (true) with check (true);

drop policy if exists "Public access to members" on public.members;
create policy "Public access to members" on public.members for all using (true) with check (true);

drop policy if exists "Public access to expenses" on public.expenses;
create policy "Public access to expenses" on public.expenses for all using (true) with check (true);

drop policy if exists "Public access to settlement_payments" on public.settlement_payments;
create policy "Public access to settlement_payments" on public.settlement_payments for all using (true) with check (true);
"""

@Composable
fun SupabaseStatusDialog(
    viewModel: TourViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val syncStatus by viewModel.supabaseSyncStatus.collectAsState()
    val testResult by viewModel.supabaseTestResult.collectAsState()
    var isTesting by remember { mutableStateOf(false) }
    var showSqlCode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF3ECF8E).copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color(0xFF2E9A68),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Supabase Backend", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Cloud Database & Sync", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Connection Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Project ID", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                color = Color(0xFF3ECF8E).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "mcvnyelqcztmffknpwkw",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B6B44),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Endpoint", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            SupabaseConfig.supabaseUrl,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color(0xFF2E9A68),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                syncStatus,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Test live connection button
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        viewModel.testSupabaseConnection { success, message ->
                            isTesting = false
                            val toastMsg = if (success) "✓ $message" else "Supabase: $message"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_supabase_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing Connection...")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Live Connection")
                    }
                }

                if (testResult != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = testResult ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Synced Entities Explanation
                Text("Saved to Supabase:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                val syncedItems = listOf(
                    "👤 User Registrations" to "public.profiles (ID, Name, Email, Login ID)",
                    "🗺️ Tours & Budgets" to "public.tours (Title, Dates, Budget, Theme)",
                    "👥 Tour Members" to "public.members (Name, Weights, Family Tag)",
                    "💵 Expenses & Splits" to "public.expenses (Paid by, Split logic, Notes)",
                    "🤝 Settlement Payments" to "public.settlement_payments (Direct debt settlements)"
                )

                syncedItems.forEach { (title, subtitle) ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(subtitle, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Copy SQL Schema action
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(SUPABASE_SQL_SCHEMA))
                        Toast.makeText(context, "SQL Schema copied! Paste into Supabase SQL Editor.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("copy_sql_schema_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Supabase SQL Schema", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { showSqlCode = !showSqlCode },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showSqlCode) "Hide SQL Code" else "View SQL Schema Code")
                }

                if (showSqlCode) {
                    Surface(
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = SUPABASE_SQL_SCHEMA,
                            color = Color(0xFFD4D4D4),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Done")
            }
        }
    )
}
