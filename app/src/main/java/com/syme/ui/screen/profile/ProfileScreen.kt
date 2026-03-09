package com.syme.ui.screen.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Man
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Woman
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syme.R
import com.syme.domain.model.User
import com.syme.domain.model.UserEvent
import com.syme.ui.component.compositionlocal.LocalCurrentUserSession
import com.syme.ui.state.UiState
import com.syme.ui.theme.AvatarFemaleBg
import com.syme.ui.theme.AvatarFemaleFg
import com.syme.ui.theme.AvatarMaleBg
import com.syme.ui.theme.AvatarMaleFg
import com.syme.ui.theme.AvatarOtherBg
import com.syme.ui.theme.AvatarOtherFg
import com.syme.ui.viewmodel.UserViewModel
import com.syme.utils.TimeUtils.formatDate2
import com.syme.utils.TimeUtils.formatDateTime

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(userViewModel: UserViewModel) {
    val currentUser = LocalCurrentUserSession.current
    val context     = LocalContext.current
    val userState   by userViewModel.userState.collectAsState()

    LaunchedEffect(currentUser?.userId) {
        currentUser?.userId?.let { userViewModel.loadUser(it) }
    }

    LaunchedEffect(Unit) {
        userViewModel.userEvent.collect { event ->
            when (event) {
                is UserEvent.Success ->
                    Toast.makeText(context, context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
                is UserEvent.Error   ->
                    Toast.makeText(context, event.arg ?: context.getString(event.messageRes), Toast.LENGTH_SHORT).show()
            }
        }
    }

    when (userState) {
        is UiState.Loading, UiState.Idle -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text((userState as UiState.Error).message)
            }
        }
        is UiState.Success -> {
            val user = (userState as UiState.Success<User>).data
            ProfileContent(
                user = user,
                onUpdatePersonal = { firstName, lastName, birthday, gender ->
                    currentUser?.userId?.let {
                        userViewModel.updateUser(
                            currentUserId = it,
                            firstName     = firstName,
                            lastName      = lastName,
                            birthday      = birthday,
                            gender        = gender
                        )
                    }
                },
                onUpdateContact = { phone, address ->
                    currentUser?.userId?.let {
                        userViewModel.updateUser(
                            currentUserId = it,
                            phone         = phone,
                            address       = address
                        )
                    }
                }
            )
        }
        else -> {}
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    user: User,
    onUpdatePersonal: (String, String, Long?, String) -> Unit,
    onUpdateContact: (String, String) -> Unit
) {
    var showPersonalDialog by remember { mutableStateOf(false) }
    var showContactDialog  by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { ProfileAvatar(user) }

        // ── Personal — editable ────────────────────────────────────────────────
        item {
            ProfileSection(
                title    = stringResource(R.string.profile_section_personal),
                editable = true,
                onClick  = { showPersonalDialog = true }
            ) {
                ProfileInfoRow(Icons.Outlined.Person,   stringResource(R.string.profile_label_full_name), "${user.firstName} ${user.lastName}".trim().ifBlank { "—" })
                ProfileDivider()
                ProfileInfoRow(Icons.Outlined.Cake,     stringResource(R.string.profile_label_birthday),  user.birthday?.let { formatDate2(it) } ?: "—")
                ProfileDivider()
                ProfileInfoRow(genderIcon(user.gender), stringResource(R.string.profile_label_gender),    formatGender(user.gender))
            }
        }

        // ── Contact — editable ────────────────────────────────────────────────
        item {
            ProfileSection(
                title    = stringResource(R.string.profile_section_contact),
                editable = true,
                onClick  = { showContactDialog = true }
            ) {
                ProfileInfoRow(Icons.Outlined.Email,      stringResource(R.string.profile_label_email),   user.email.ifBlank { "—" })
                ProfileDivider()
                ProfileInfoRow(Icons.Outlined.Phone,      stringResource(R.string.profile_label_phone),   user.phone.ifBlank { "—" })
                ProfileDivider()
                ProfileInfoRow(Icons.Outlined.LocationOn, stringResource(R.string.profile_label_address), user.address.ifBlank { "—" })
            }
        }

        // ── Roles — read-only ─────────────────────────────────────────────────
        if (!user.roles.isNullOrEmpty()) {
            item {
                ProfileSection(title = stringResource(R.string.profile_section_roles)) {
                    user.roles.entries.forEachIndexed { index, (role, active) ->
                        RoleRow(role, active)
                        if (index < user.roles.size - 1) ProfileDivider()
                    }
                }
            }
        }

        // ── Traceability — read-only ──────────────────────────────────────────
        item {
            ProfileSection(title = stringResource(R.string.profile_section_traceability)) {
                ProfileInfoRow(Icons.Outlined.AddCircleOutline, stringResource(R.string.profile_label_created_at), formatDateTime(user.trace.createdAt))
                ProfileDivider()
                ProfileInfoRow(Icons.Outlined.EditCalendar,     stringResource(R.string.profile_label_updated_at), formatDateTime(user.trace.updatedAt))
                ProfileDivider()
                ProfileInfoRow(Icons.Outlined.Tag,              stringResource(R.string.profile_label_version),    "v${user.trace.version}")
                ProfileDivider()
                ProfileInfoRow(
                    icon       = if (user.trace.active) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                    label      = stringResource(R.string.profile_label_status),
                    value      = if (user.trace.active) stringResource(R.string.profile_status_active) else stringResource(R.string.profile_status_inactive),
                    valueColor = if (user.trace.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // ── Edit dialogs ──────────────────────────────────────────────────────────

    if (showPersonalDialog) {
        EditPersonalDialog(
            user      = user,
            onDismiss = { showPersonalDialog = false },
            onConfirm = { firstName, lastName, birthday, gender ->
                onUpdatePersonal(firstName, lastName, birthday, gender)
                showPersonalDialog = false
            }
        )
    }

    if (showContactDialog) {
        EditContactDialog(
            user      = user,
            onDismiss = { showContactDialog = false },
            onConfirm = { phone, address ->
                onUpdateContact(phone, address)
                showContactDialog = false
            }
        )
    }
}

// ─── Edit — Personal ──────────────────────────────────────────────────────────

@Composable
private fun EditPersonalDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, String) -> Unit
) {
    var firstName by remember { mutableStateOf(user.firstName) }
    var lastName  by remember { mutableStateOf(user.lastName) }
    var gender    by remember { mutableStateOf(user.gender) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit_personal_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = firstName,
                    onValueChange = { firstName = it },
                    label         = { Text(stringResource(R.string.profile_label_first_name)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = lastName,
                    onValueChange = { lastName = it },
                    label         = { Text(stringResource(R.string.profile_label_last_name)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
                GenderSelector(selected = gender, onSelect = { gender = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(firstName.trim(), lastName.trim(), user.birthday, gender) }) {
                Text(stringResource(R.string.profile_edit_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_edit_cancel)) }
        }
    )
}

// ─── Edit — Contact ───────────────────────────────────────────────────────────

@Composable
private fun EditContactDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var phone   by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_edit_contact_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value           = phone,
                    onValueChange   = { phone = it },
                    label           = { Text(stringResource(R.string.profile_label_phone)) },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier        = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value         = address,
                    onValueChange = { address = it },
                    label         = { Text(stringResource(R.string.profile_label_address)) },
                    maxLines      = 3,
                    modifier      = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(phone.trim(), address.trim()) }) {
                Text(stringResource(R.string.profile_edit_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_edit_cancel)) }
        }
    )
}

// ─── Gender selector ──────────────────────────────────────────────────────────

@Composable
private fun GenderSelector(selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("male", "female").forEach { option ->
            val isSelected = selected.lowercase() == option
            FilterChip(
                selected    = isSelected,
                onClick     = { onSelect(option) },
                label       = { Text(option.replaceFirstChar { it.uppercase() }) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

// ─── Section wrapper ──────────────────────────────────────────────────────────

@Composable
private fun ProfileSection(
    title: String,
    editable: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text         = title.uppercase(),
                style        = MaterialTheme.typography.labelSmall,
                fontWeight   = FontWeight.SemiBold,
                color        = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier     = Modifier.weight(1f)
            )
            if (editable) {
                Icon(
                    imageVector     = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.profile_edit_icon_desc),
                    tint            = MaterialTheme.colorScheme.primary,
                    modifier        = Modifier.size(16.dp).clickable { onClick() }
                )
            }
        }

        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier  = Modifier
                .fillMaxWidth()
                .then(if (editable) Modifier.clickable { onClick() } else Modifier)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
        Spacer(Modifier.height(4.dp))
    }
}

// ─── Info row ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
        }
    }
}

// ─── Role row ─────────────────────────────────────────────────────────────────

@Composable
private fun RoleRow(role: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(role.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(50),
            color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        ) {
            Text(
                text       = if (active) "Active" else "Inactive",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ─── Avatar ───────────────────────────────────────────────────────────────────
@Composable
private fun ProfileAvatar(user: User) {
    val (bg, fg, drawableRes) = when (user.gender.lowercase()) {
        "male", "homme", "m"   -> Triple(AvatarMaleBg, AvatarMaleFg, R.drawable.businessman_svgrepo_com)
        "female", "femme", "f" -> Triple(AvatarFemaleBg, AvatarFemaleFg, R.drawable.businesswoman_svgrepo_com)
        else                    -> Triple(AvatarOtherBg, AvatarOtherFg, R.drawable.users_svgrepo_com)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = drawableRes),
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "${user.firstName} ${user.lastName}".trim().ifBlank { "—" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (user.email.isNotBlank())
            Text(
                user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 50.dp), color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
}

@Composable
private fun genderIcon(g: String): ImageVector = when (g.lowercase()) {
    "male", "homme", "m" -> Icons.Outlined.Man
    "female", "femme", "f" -> Icons.Outlined.Woman
    else -> Icons.Outlined.Person
}

private fun formatGender(g: String) = when (g.lowercase()) {
    "male", "homme", "m" -> "Male"
    "female", "femme", "f" -> "Female"
    else -> g.ifBlank { "—" }
}