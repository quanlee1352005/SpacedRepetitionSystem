package com.example.spacedrepetitionsystem.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            AuthForm(onAuthSuccess = { user = auth.currentUser })
        } else {
            UserInfoSection(user?.email ?: userName, onSync, onLogout = {
                auth.signOut()
                user = null
                Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            })
        }
    }
}

@Composable
fun AuthForm(onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Text(
        text = if (isRegisterMode) "Tạo tài khoản mới" else "Đăng nhập tài khoản",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFE67E22)
    )

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Địa chỉ Email") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = password, 
        onValueChange = { password = it }, 
        label = { Text("Mật khẩu") }, 
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp)
    )

    Button(
        onClick = {
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@Button
            }

            if (isRegisterMode) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) onAuthSuccess()
                    else Toast.makeText(context, "Đăng ký lỗi: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) onAuthSuccess()
                    else Toast.makeText(context, "Đăng nhập lỗi: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE67E22)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(if (isRegisterMode) "Đăng ký ngay" else "Đăng nhập", fontSize = 16.sp)
    }

    TextButton(
        onClick = { isRegisterMode = !isRegisterMode },
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            if (isRegisterMode) "Đã có tài khoản? Đăng nhập ngay"
            else "Chưa có tài khoản? Tạo ngay tại đây",
            color = Color.Gray
        )
    }
}

@Composable
fun ColumnScope.UserInfoSection(email: String, onSync: () -> Unit, onLogout: () -> Unit) {
    Surface(modifier = Modifier.size(100.dp).clip(CircleShape), color = Color(0xFFFDEFD9)) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(64.dp).padding(16.dp), tint = Color(0xFFE67E22))
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = "Xin chào!", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    Text(text = email, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    
    Spacer(modifier = Modifier.height(40.dp))

    AccountMenuItem("Đồng bộ dữ liệu với Cloud", Icons.Default.CloudSync, onSync)
    AccountMenuItem("Thông tin ứng dụng", Icons.Default.Info, {})

    Spacer(modifier = Modifier.weight(1f))

    AccountMenuItem("Đăng xuất tài khoản", Icons.Default.Logout, onLogout, isCritical = true)
}

@Composable
fun AccountMenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, isCritical: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCritical) Color(0xFFFFEBEE) else Color.White,
            contentColor = if (isCritical) Color.Red else Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, fontWeight = FontWeight.Medium)
        }
    }
}
