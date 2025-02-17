package com.isaakhanimann.journal.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileInputStream

class BackupService(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Export data
        val exportFile = exportData()

        // Upload to FTP
        uploadToFTP(exportFile)

        // Upload to SFTP
        uploadToSFTP(exportFile)

        return Result.success()
    }

    private fun exportData(): File {
        // Implement the export functionality here
        // For example, use the existing export functionality to generate the backup file
        // Return the generated file
    }

    private fun uploadToFTP(file: File) {
        val ftpClient = FTPClient()
        try {
            ftpClient.connect("ftp.example.com", 21)
            ftpClient.login("username", "password")
            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

            val inputStream = FileInputStream(file)
            ftpClient.storeFile("/backup/${file.name}", inputStream)
            inputStream.close()

            ftpClient.logout()
            ftpClient.disconnect()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun uploadToSFTP(file: File) {
        val jsch = JSch()
        val session: Session = jsch.getSession("username", "sftp.example.com", 22)
        session.setPassword("password")
        session.setConfig("StrictHostKeyChecking", "no")
        session.connect()

        val channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()

        val inputStream = FileInputStream(file)
        channel.put(inputStream, "/backup/${file.name}")
        inputStream.close()

        channel.disconnect()
        session.disconnect()
    }
}