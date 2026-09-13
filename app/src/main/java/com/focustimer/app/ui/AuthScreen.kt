package com.focustimer.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.focustimer.app.PrefsManager
import com.focustimer.app.sha256

@Composable
fun AuthScreen(startInLoginMode: Boolean, onAuthenticated: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { PrefsManager(context) }

    var isLoginMode by remember { mutableStateOf(startInLoginMode) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }

    var loginEmail by remember { mutableStateOf(prefs.email) }
    var loginPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "Focus Timer",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Личный кабинет для контроля времени",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentButton("Вход", isLoginMode, Modifier.weight(1f)) {
                isLoginMode = true
                errorMessage = null
            }
            SegmentButton("Регистрация", !isLoginMode, Modifier.weight(1f)) {
                isLoginMode = false
                errorMessage = null
            }
        }

        if (isLoginMode) {
            OutlinedTextField(
                value = loginEmail,
                onValueChange = { loginEmail = it },
                label = { Text("Почта") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }

            Button(
                onClick = {
                    when {
                        !prefs.isRegistered -> errorMessage = "Аккаунт не найден — зарегистрируйтесь"
                        loginEmail.trim().equals(prefs.email.trim(), ignoreCase = true) &&
                            sha256(loginPassword) == prefs.accountPasswordHash -> {
                            prefs.isLoggedIn = true
                            onAuthenticated()
                        }
                        else -> errorMessage = "Неверная почта или пароль"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Войти")
            }
        } else {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            OutlinedTextField(
                value = regEmail,
                onValueChange = { regEmail = it },
                label = { Text("Почта") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            OutlinedTextField(
                value = regPassword,
                onValueChange = { regPassword = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable { consent = !consent },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = consent, onCheckedChange = { consent = it })
                Text("Согласен(на) на обработку персональных данных")
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }

            Button(
                onClick = {
                    when {
                        firstName.isBlank() || regEmail.isBlank() || regPassword.isBlank() ->
                            errorMessage = "Заполните имя, почту и пароль"
                        !consent -> errorMessage = "Нужно согласие на обработку данных"
                        else -> {
                            prefs.userName = firstName
                            prefs.lastName = lastName
                            prefs.email = regEmail
                            prefs.accountPasswordHash = sha256(regPassword)
                            prefs.dataConsentGiven = true
                            prefs.isRegistered = true
                            prefs.isLoggedIn = true
                            onAuthenticated()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Зарегистрироваться")
            }
        }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}
