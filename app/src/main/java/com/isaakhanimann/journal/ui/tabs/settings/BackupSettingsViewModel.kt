package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.net.ftp.FTPClient
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsViewModel: SettingsViewModel // Inject SettingsViewModel
) : ViewModel() {

    fun backupToFTP(server: String, port: Int, user: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportFile = SettingsViewModel.exportData() // Call export function
            val ftpClient = FTPClient()
            try {
                ftpClient.connect(server, port)
                ftpClient.login(user, pass)
                ftpClient.enterLocalPassiveMode()
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)

                val inputStream = FileInputStream(exportFile)
                val done = ftpClient.storeFile(exportFile.name, inputStream)
                inputStream.close()
                if (done) {
                    // Handle success
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                ftpClient.logout()
                ftpClient.disconnect()
            }
        }
    }

    fun backupToSFTP(server: String, port: Int, user: String, pass: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportFile = settingsViewModel.exportData() // Call export function
            val jsch = JSch()
            var session: Session? = null
            var channel: ChannelSftp? = null
            try {
                session = jsch.getSession(user, server, port)
                session.setPassword(pass)
                session.setConfig("StrictHostKeyChecking", "no")
                session.connect()

                channel = session.openChannel("sftp") as ChannelSftp
                channel.connect()

                val inputStream = FileInputStream(exportFile)
                channel.put(inputStream, exportFile.name)
                inputStream.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                channel?.disconnect()
                session?.disconnect()
            }
        }
    }

    fun backupToGoogleDrive() {
        // Implement Google Drive backup logic here
    }

    fun scheduleAutomaticBackup() {
        // Implement scheduling logic here
    }
}