package com.example.spacedrepetitionsystem.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AccountScreen(
    userName: String,
    onSync: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    var user by remember { mutableStateOf(auth.currentUser) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null || user?.isAnonymous == true) {
            // Giao diện Đăng nhập
            LoginSection(onLoginSuccess = { user = auth.currentUser })
        } else {
            // Giao diện Thông tin tài khoản (Đã đăng nhập)
            UserInfoSection(user?.email ?: userName, onSync, onLogout = {
                auth.signOut()
                user = null
                Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun LoginSection(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Text("Đăng nhập để đồng bộ dữ liệu", style = MaterialTheme.typography.titleLarge)
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(
        value = password, 
        onValueChange = { password = it }, 
        label = { Text("Mật khẩu") }, 
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation()
    )

    Button(
        onClick = {
            if (email.isNotBlank() && password.isNotBlank()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) onLoginSuccess()
                    else Toast.makeText(context, "Lỗi: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) { Text("Đăng nhập") }

    TextButton(onClick = {
        if (email.isNotBlank() && password.isNotBlank()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) onLoginSuccess()
                else Toast.makeText(context, "Lỗi: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }) { Text("Chưa có tài khoản? Đăng ký ngay") }
}

@Composable
fun UserInfoSection(email: String, onSync: () -> Unit, onLogout: () -> Unit) {
    Surface(modifier = Modifier.size(100.dp).clip(CircleShape), color = MaterialTheme.colorScheme.primaryContainer) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp).padding(16.dp), tint = MaterialTheme.colorScheme.primary)
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = email, style = MaterialTheme.typography.headlineSmall)
    
    Spacer(modifier = Modifier.height(32.dp))
    
    AccountMenuItem("Đồng bộ dữ liệu Cloud", Icons.Default.CloudSync, onSync)
    AccountMenuItem("Đăng xuất", Icons.Default.Logout, onLogout, isCritical = true)
}

@Composable
fun AccountMenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, isCritical: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isCritical) Color(0xFFFFEBEE) else Color.White, contentColor = if (isCritical) Color.Red else Color.Black),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label)
        }
    }
}
