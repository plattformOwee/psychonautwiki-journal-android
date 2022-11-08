package com.isaakhanimann.journal.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.journal.BuildConfig
import com.isaakhanimann.journal.ui.theme.horizontalPadding

@Preview
@Composable
fun SettingsPreview() {
    SettingsScreen(
        deleteEverything = {},
        navigateToFAQ = {},
        navigateToComboSettings = {},
        importFile = {},
        exportFile = {}
    )
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit
) {
    SettingsScreen(
        navigateToFAQ = navigateToFAQ,
        navigateToComboSettings = navigateToComboSettings,
        deleteEverything = viewModel::deleteEverything,
        importFile = viewModel::importFile,
        exportFile = viewModel::exportFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateToFAQ: () -> Unit,
    navigateToComboSettings: () -> Unit,
    deleteEverything: () -> Unit,
    importFile: (uri: Uri) -> Unit,
    exportFile: (uri: Uri) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            SettingsButton(
                imageVector = Icons.Outlined.WarningAmber,
                text = "Interaction Settings"
            ) {
                navigateToComboSettings()
            }
            Divider()
            val uriHandler = LocalUriHandler.current
            SettingsButton(
                imageVector = Icons.Outlined.OpenInBrowser,
                text = "Responsible Drug Use"
            ) {
                uriHandler.openUri("https://psychonautwiki.org/wiki/Responsible_drug_use")
            }
            Divider()
            SettingsButton(imageVector = Icons.Outlined.QuestionAnswer, text = "FAQ") {
                navigateToFAQ()
            }
            Divider()
            SettingsButton(
                imageVector = Icons.Outlined.ContactSupport,
                text = "Question / Feedback / Bug Report"
            ) {
                uriHandler.openUri("https://t.me/isaakhanimann")
            }
            Divider()
            SettingsButton(imageVector = Icons.Outlined.Code, text = "Source Code") {
                uriHandler.openUri("https://github.com/isaakhanimann/HealthAssistant")
            }
            Divider()
            SettingsButton(imageVector = Icons.Outlined.VolunteerActivism, text = "Donate") {
                uriHandler.openUri("https://www.buymeacoffee.com/isaakhanimann")
            }
            Divider()
            var isShowingExportDialog by remember { mutableStateOf(false) }
            SettingsButton(imageVector = Icons.Outlined.FileUpload, text = "Export File") {
                isShowingExportDialog = true
            }
            if (isShowingExportDialog) {
                val launcherExport =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(
                            mimeType = "application/json"
                        )
                    ) { uri ->
                        if (uri != null) {
                            exportFile(uri)
                        }
                    }
                AlertDialog(
                    onDismissRequest = { isShowingExportDialog = false },
                    title = {
                        Text(text = "Export?")
                    },
                    text = {
                        Text("This will export all your data from the app into a file so you can send it to someone or import it again on a new phone")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isShowingExportDialog = false
                                launcherExport.launch("Journal.json")
                            }
                        ) {
                            Text("Export")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isShowingExportDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            Divider()
            var isShowingImportDialog by remember { mutableStateOf(false) }
            SettingsButton(imageVector = Icons.Outlined.FileDownload, text = "Import File") {
                isShowingImportDialog = true
            }
            val launcherImport =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        importFile(uri)
                    }
                }
            if (isShowingImportDialog) {
                AlertDialog(
                    onDismissRequest = { isShowingImportDialog = false },
                    title = {
                        Text(text = "Import File?")
                    },
                    text = {
                        Text("Import a file that was exported before. Note that this won't delete the data that you already have in the app, so consider deleting everything first.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isShowingImportDialog = false
                                launcherImport.launch("*/*")
                            }
                        ) {
                            Text("Import")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isShowingImportDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            Divider()
            var isShowingDeleteDialog by remember { mutableStateOf(false) }
            SettingsButton(imageVector = Icons.Outlined.DeleteForever, text = "Delete Everything") {
                isShowingDeleteDialog = true
            }
            if (isShowingDeleteDialog) {
                val context = LocalContext.current
                AlertDialog(
                    onDismissRequest = { isShowingDeleteDialog = false },
                    title = {
                        Text(text = "Delete Everything?")
                    },
                    text = {
                        Text("This will delete all your experiences, ingestions and custom substances.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                isShowingDeleteDialog = false
                                deleteEverything()
                                Toast.makeText(
                                    context,
                                    "Everything Deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { isShowingDeleteDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            Divider()
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)) {
                Text(
                    text = "Version " + BuildConfig.VERSION_NAME,
                )
                Text(
                    text = "Last Substance Update: 25. October 2022",
                )
            }
        }
    }
}

@Composable
fun SettingsButton(imageVector: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = horizontalPadding)
    ) {
        Icon(
            imageVector,
            contentDescription = imageVector.name,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(text)
        Spacer(modifier = Modifier.weight(1f))
    }
}