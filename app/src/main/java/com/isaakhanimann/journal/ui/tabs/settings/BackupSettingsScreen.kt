package com.isaakhanimann.journal.ui.tabs.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BackupSettingsScreen(viewModel: BackupSettingsViewModel = hiltViewModel()) {
    var isFTPEnabled by remember { mutableStateOf(false) }
    var isSFTPEnabled by remember { mutableStateOf(false) }
    var isGoogleDriveEnabled by remember { mutableStateOf(false) }

    var ftpServer by remember { mutableStateOf("") }
    var ftpPort by remember { mutableStateOf("") }
    var ftpUser by remember { mutableStateOf("") }
    var ftpPass by remember { mutableStateOf("") }

    var sftpServer by remember { mutableStateOf("") }
    var sftpPort by remember { mutableStateOf("") }
    var sftpUser by remember { mutableStateOf("") }
    var sftpPass by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Backup Settings", style = MaterialTheme.typography.h6)

        SwitchSetting(
            label = "Enable FTP Backup",
            checked = isFTPEnabled,
            onCheckedChange = { isFTPEnabled = it }
        )

        if (isFTPEnabled) {
            TextField(value = ftpServer, onValueChange = { ftpServer = it }, label = { Text("FTP Server") })
            TextField(value = ftpPort, onValueChange = { ftpPort = it }, label = { Text("FTP Port") })
            TextField(value = ftpUser, onValueChange = { ftpUser = it }, label = { Text("FTP User") })
            TextField(value = ftpPass, onValueChange = { ftpPass = it }, label = { Text("FTP Password") }, visualTransformation = PasswordVisualTransformation())
            Button(onClick = { viewModel.backupToFTP(ftpServer, ftpPort.toInt(), ftpUser, ftpPass) }) {
                Text("Backup Now")
            }
        }

        SwitchSetting(
            label = "Enable SFTP Backup",
            checked = isSFTPEnabled,
            onCheckedChange = { isSFTPEnabled = it }
        )

        if (isSFTPEnabled) {
            TextField(value = sftpServer, onValueChange = { sftpServer = it }, label = { Text("SFTP Server") })
            TextField(value = sftpPort, onValueChange = { sftpPort = it }, label = { Text("SFTP Port") })
            TextField(value = sftpUser, onValueChange = { sftpUser = it }, label = { Text("SFTP User") })
            TextField(value = sftpPass, onValueChange = { sftpPass = it }, label = { Text("SFTP Password") }, visualTransformation = PasswordVisualTransformation())
            Button(onClick = { viewModel.backupToSFTP(sftpServer, sftpPort.toInt(), sftpUser, sftpPass) }) {
                Text("Backup Now")
            }
        }

        SwitchSetting(
            label = "Enable Google Drive Backup (Dummy)",
            checked = isGoogleDriveEnabled,
            onCheckedChange = { isGoogleDriveEnabled = it }
        )
    }
}

@Composable
fun SwitchSetting(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}