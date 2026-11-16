package com.privatecloset.app.ui.marketplace

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.MarketplaceCategory
import com.privatecloset.app.data.model.MarketplaceItem
import com.privatecloset.app.data.model.PaymentMethod
import com.privatecloset.app.data.model.PaymentTransaction
import com.privatecloset.app.data.model.UserRole
import com.privatecloset.app.ui.common.StateScaffold
import com.privatecloset.app.ui.common.UiState
import com.privatecloset.app.ui.practice.PianoSoundSynthesizer
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MarketplaceScreen(
    uiState: UiState<MarketplaceUiData>,
    onSelectCategory: (MarketplaceCategory) -> Unit,
    onSearch: (String) -> Unit,
    onPurchase: (String, PaymentMethod, String, (Result<PaymentTransaction>) -> Unit) -> Unit,
    onPublish: (MarketplaceItem, (Result<MarketplaceItem>) -> Unit) -> Unit,
) {
    var purchasingItem by remember { mutableStateOf<MarketplaceItem?>(null) }
    var showSellModal by remember { mutableStateOf(false) }
    var successReceipt by remember { mutableStateOf<PaymentTransaction?>(null) }
    val synthesizer = remember { PianoSoundSynthesizer() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        // Header with "Sell Your Asset" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Sound & Beat Marketplace",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Buy & sell melodies, voices, beats & presets",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }

            Button(
                onClick = { showSellModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sell", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        StateScaffold(uiState) { data ->
            // Search Input
            OutlinedTextField(
                value = data.searchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search chords, presets, melodies, BPM...", color = TextSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GoldLight) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = BgStroke,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(MarketplaceCategory.values()) { category ->
                    val isSelected = category == data.selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PurpleLight else BgCard)
                            .border(1.dp, if (isSelected) Gold else BgStroke, RoundedCornerShape(20.dp))
                            .clickable { onSelectCategory(category) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = category.displayName,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            val filteredItems = data.items.filter { item ->
                (data.selectedCategory == MarketplaceCategory.ALL || item.category == data.selectedCategory) &&
                        (data.searchQuery.isBlank() || item.title.contains(data.searchQuery, ignoreCase = true) ||
                                item.tags.any { it.contains(data.searchQuery, ignoreCase = true) })
            }

            // Marketplace Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(filteredItems) { item ->
                    MarketplaceCard(
                        item = item,
                        onPlayPreview = {
                            scope.launch {
                                // Synthesize an expressive musical preview chord
                                val notes = listOf("C4", "E4", "G4", "B4", "D5")
                                notes.forEach { n ->
                                    synthesizer.playNote(synthesizer.noteNameToFrequency(n), durationMs = 900, sustain = true)
                                    delay(160)
                                }
                            }
                        },
                        onBuyClick = { purchasingItem = item }
                    )
                }
            }
        }
    }

    // --- Multi-Payment Options Modal ---
    if (purchasingItem != null) {
        PaymentOptionsDialog(
            item = purchasingItem!!,
            onDismiss = { purchasingItem = null },
            onConfirmPurchase = { method, details ->
                val targetId = purchasingItem!!.id
                onPurchase(targetId, method, details) { result ->
                    purchasingItem = null
                    if (result.isSuccess) {
                        successReceipt = result.getOrNull()
                    }
                }
            }
        )
    }

    // --- Success Receipt Modal ---
    if (successReceipt != null) {
        AlertDialog(
            onDismissRequest = { successReceipt = null },
            containerColor = BgCard,
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(36.dp)) },
            title = { Text("Purchase Confirmed!", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your asset has been unlocked and added to your Private Closet library.", color = TextSecondary, fontSize = 13.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1A34D399))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Transaction: ${successReceipt!!.id}", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Item: ${successReceipt!!.itemTitle}", color = TextPrimary, fontSize = 12.sp)
                            Text("Paid: $${String.format("%.2f", successReceipt!!.amountUsd)} via ${successReceipt!!.paymentMethod.title}", color = TextSecondary, fontSize = 11.sp)
                            successReceipt!!.registeredDetails?.takeIf { it.isNotBlank() }?.let { details ->
                                Text("Account: $details", color = GoldLight, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { successReceipt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Download Asset", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // --- Sell Asset Modal ---
    if (showSellModal) {
        SellAssetDialog(
            onDismiss = { showSellModal = false },
            onPublish = { newItem ->
                onPublish(newItem) {
                    showSellModal = false
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MarketplaceCard(
    item: MarketplaceItem,
    onPlayPreview: () -> Unit,
    onBuyClick: () -> Unit,
) {
    val isTutor = item.sellerRole == UserRole.INSTRUCTOR

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BgStroke, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        // Top row: Category tag & Price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Purple.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(item.category.displayName, color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "$${String.format("%.2f", item.priceUsd)}",
                color = Gold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(8.dp))

        // Title
        Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(4.dp))

        // Description
        Text(
            text = item.description,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )

        Spacer(Modifier.height(10.dp))

        // Tags & Details (BPM, Key)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item.bpm?.let {
                DetailBadge(text = "$it BPM")
            }
            item.keyNote?.let {
                DetailBadge(text = "Key: $it")
            }
            item.tags.forEach { tag ->
                DetailBadge(text = "#$tag")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Bottom Row: Creator info & Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("by ${item.sellerName}", color = TextSecondary, fontSize = 11.sp)
                if (isTutor) {
                    Icon(Icons.Filled.Verified, contentDescription = "Verified Tutor", tint = Gold, modifier = Modifier.size(13.dp))
                }
                Text("· ★ ${item.rating}", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPlayPreview,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Audio", fontSize = 11.sp)
                }

                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isPurchased) Color(0xFF34D399) else Gold,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (item.isPurchased) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Owned", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    } else {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Buy", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BgStroke)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun PaymentOptionsDialog(
    item: MarketplaceItem,
    onDismiss: () -> Unit,
    onConfirmPurchase: (PaymentMethod, String) -> Unit,
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CARD) }
    var currentStep by remember { mutableStateOf(0) } // 0 = Gateway Select, 1 = Register Payment Details
    var isProcessing by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Card form state
    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var billingZip by remember { mutableStateOf("") }

    // Google Pay state
    var gpayEmail by remember { mutableStateOf("mthuthuzelindwendwa182@gmail.com") }
    var gpayPhone by remember { mutableStateOf("") }

    // PayPal state
    var paypalEmail by remember { mutableStateOf("") }
    var paypalFunding by remember { mutableStateOf("PayPal Balance & Linked Bank") }

    // M-Pesa / Mobile Money state
    var mpesaOperator by remember { mutableStateOf("M-Pesa / Vodacom") }
    var mpesaPhone by remember { mutableStateOf("") }
    var mpesaPinConfirmed by remember { mutableStateOf(false) }

    // Crypto state
    var cryptoNetwork by remember { mutableStateOf("Ethereum (USDC / ETH)") }
    var cryptoWallet by remember { mutableStateOf("") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF140D22),
        unfocusedContainerColor = Color(0xFF140D22),
        focusedBorderColor = Gold,
        unfocusedBorderColor = BgStroke,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedLabelColor = GoldLight,
        unfocusedLabelColor = TextSecondary,
    )

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        containerColor = BgCard,
        title = {
            Text(
                text = if (currentStep == 0) "Select Payment Option" else "Register ${selectedMethod.title}",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1AD4AF37))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Creator: ${item.sellerName}", color = TextSecondary, fontSize = 11.sp)
                        }
                        Text("$${String.format("%.2f", item.priceUsd)}", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentStep == 0) {
                    Text("Choose a gateway to enter your payment details:", color = TextSecondary, fontSize = 12.sp)
                    PaymentMethod.values().forEach { method ->
                        val isSelected = method == selectedMethod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Purple.copy(alpha = 0.35f) else Color(0xFF140D22))
                                .border(1.dp, if (isSelected) Gold else BgStroke, RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedMethod = method
                                    errorMsg = null
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(method.iconRes, fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(method.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(method.subtitle, color = TextSecondary, fontSize = 10.sp)
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedMethod = method
                                    errorMsg = null
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Gold, unselectedColor = TextSecondary)
                            )
                        }
                    }
                } else {
                    when (selectedMethod) {
                        PaymentMethod.CARD -> {
                            OutlinedTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it; errorMsg = null },
                                label = { Text("Cardholder Name") },
                                placeholder = { Text("e.g. Mthuthuzeli Ndwendwa") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it.filter { ch -> ch.isDigit() || ch == ' ' }.take(19); errorMsg = null },
                                label = { Text("Card Number") },
                                placeholder = { Text("4532 •••• •••• 8821") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { cardExpiry = it.take(5); errorMsg = null },
                                    label = { Text("MM/YY") },
                                    placeholder = { Text("09/28") },
                                    singleLine = true,
                                    colors = textFieldColors,
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { cardCvv = it.filter(Char::isDigit).take(4); errorMsg = null },
                                    label = { Text("CVC") },
                                    placeholder = { Text("842") },
                                    singleLine = true,
                                    colors = textFieldColors,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            OutlinedTextField(
                                value = billingZip,
                                onValueChange = { billingZip = it.take(12); errorMsg = null },
                                label = { Text("Billing Postal / ZIP Code") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        PaymentMethod.GOOGLE_PAY -> {
                            OutlinedTextField(
                                value = gpayEmail,
                                onValueChange = { gpayEmail = it; errorMsg = null },
                                label = { Text("Google Account Email") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = gpayPhone,
                                onValueChange = { gpayPhone = it; errorMsg = null },
                                label = { Text("Verification Phone Number") },
                                placeholder = { Text("+27 82 000 0000") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text("Google Wallet biometric authorization will be confirmed on pay.", color = TextSecondary, fontSize = 11.sp)
                        }
                        PaymentMethod.PAYPAL -> {
                            OutlinedTextField(
                                value = paypalEmail,
                                onValueChange = { paypalEmail = it; errorMsg = null },
                                label = { Text("PayPal Account Email") },
                                placeholder = { Text("you@example.com") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = paypalFunding,
                                onValueChange = { paypalFunding = it; errorMsg = null },
                                label = { Text("Funding Source") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        PaymentMethod.MPESA -> {
                            OutlinedTextField(
                                value = mpesaOperator,
                                onValueChange = { mpesaOperator = it; errorMsg = null },
                                label = { Text("Mobile Network Operator") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = mpesaPhone,
                                onValueChange = { mpesaPhone = it; errorMsg = null },
                                label = { Text("Mobile Money Number") },
                                placeholder = { Text("+254 712 345 678") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedButton(
                                onClick = { mpesaPinConfirmed = true; errorMsg = null },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text(
                                    if (mpesaPinConfirmed) "✓ STK Push Prompt Sent & Approved" else "Send STK Push Prompt to Phone",
                                    color = if (mpesaPinConfirmed) Color(0xFF34D399) else GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        PaymentMethod.CRYPTO -> {
                            OutlinedTextField(
                                value = cryptoNetwork,
                                onValueChange = { cryptoNetwork = it; errorMsg = null },
                                label = { Text("Blockchain Network") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = cryptoWallet,
                                onValueChange = { cryptoWallet = it; errorMsg = null },
                                label = { Text("Sender Wallet Address") },
                                placeholder = { Text("0x71C...89A2 or Solana address") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text("Deposit Address: 0xPrivateClosetVault94F8E2", color = GoldLight, fontSize = 11.sp)
                        }
                    }

                    errorMsg?.let { err ->
                        Text(err, color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = Gold, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Authorizing ${selectedMethod.title}...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (currentStep == 0) {
                Button(
                    onClick = { currentStep = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
                ) {
                    Text("Enter Details →", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        val detailsSummary: String? = when (selectedMethod) {
                            PaymentMethod.CARD -> {
                                val digits = cardNumber.filter(Char::isDigit)
                                if (cardHolder.isBlank() || digits.length < 12 || cardExpiry.length < 4 || cardCvv.length < 3) {
                                    errorMsg = "Please enter valid cardholder name, card number, expiry, and CVC."
                                    null
                                } else {
                                    "Card •••• ${digits.takeLast(4)} ($cardHolder)"
                                }
                            }
                            PaymentMethod.GOOGLE_PAY -> {
                                if (!gpayEmail.contains("@")) {
                                    errorMsg = "Please enter a valid Google Account email."
                                    null
                                } else {
                                    "Google Pay ($gpayEmail)"
                                }
                            }
                            PaymentMethod.PAYPAL -> {
                                if (!paypalEmail.contains("@")) {
                                    errorMsg = "Please enter your PayPal email address."
                                    null
                                } else {
                                    "PayPal ($paypalEmail)"
                                }
                            }
                            PaymentMethod.MPESA -> {
                                if (mpesaPhone.filter(Char::isDigit).length < 8) {
                                    errorMsg = "Please enter a valid mobile money number and approve STK push."
                                    null
                                } else {
                                    "$mpesaOperator ($mpesaPhone)"
                                }
                            }
                            PaymentMethod.CRYPTO -> {
                                if (cryptoWallet.trim().length < 10) {
                                    errorMsg = "Please enter your sending wallet address."
                                    null
                                } else {
                                    "$cryptoNetwork (${cryptoWallet.take(8)}...)"
                                }
                            }
                        }
                        if (detailsSummary != null) {
                            isProcessing = true
                            onConfirmPurchase(selectedMethod, detailsSummary)
                        }
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
                ) {
                    Text("Confirm & Pay $${String.format("%.2f", item.priceUsd)}", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (currentStep == 1 && !isProcessing) {
                TextButton(onClick = { currentStep = 0; errorMsg = null }) {
                    Text("← Back", color = TextSecondary)
                }
            } else {
                TextButton(onClick = onDismiss, enabled = !isProcessing) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}

@Composable
private fun SellAssetDialog(
    onDismiss: () -> Unit,
    onPublish: (MarketplaceItem) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("15.00") }
    var category by remember { mutableStateOf(MarketplaceCategory.MELODIES) }
    var tagsInput by remember { mutableStateOf("WAV, MIDI, 110 BPM") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        title = { Text("List Your Sound on Marketplace", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Asset Title") },
                    placeholder = { Text("e.g. Soulful Gospel Progressions Vol 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe keys, instruments, format...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (USD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val parsedPrice = priceText.toDoubleOrNull() ?: 15.0
                        val tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val newItem = MarketplaceItem(
                            id = "",
                            title = title,
                            description = description.ifBlank { "High-quality keyboard asset pack." },
                            category = category,
                            priceUsd = parsedPrice,
                            sellerId = "",
                            sellerName = "",
                            sellerRole = UserRole.STUDENT,
                            rating = 5.0,
                            salesCount = 0,
                            tags = tags.ifEmpty { listOf("Keys", "Audio") },
                        )
                        onPublish(newItem)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)
            ) {
                Text("Publish to Market", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
