package com.micmarsh.budget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.micmarsh.budget.ui.components.*


import com.micmarsh.budget.ui.theme.BudgetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = SettingsStorage.create(this)
        val repo = SyncableMessageRepository.create(this)
        val resources = this.resources
        setContent() {
            BudgetTheme {
                settingsManagement(storage, repo)
                smsPermissionDialog(resources, storage)
            }
        }
    }
}