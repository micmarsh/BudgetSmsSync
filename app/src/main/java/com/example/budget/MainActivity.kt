package com.example.budget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budget.ui.theme.BudgetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var smsServiceIntent = Intent(this, TextListenerService::class.java)

        setContent() {
            BudgetTheme {
                Scaffold {innerPadding ->
                    Column(Modifier
                        .padding(innerPadding)
                        .fillMaxSize()) {
                        Text(fontSize = 30.sp, text = "Settings")
                        phoneNumberList()
                    }
                }
            }
        }
        // startService(smsServiceIntent)
    }
}

@Composable
fun phoneNumberList() {
    val sourceNumbers = remember {
        mutableStateListOf<String>() // todo some (already) validated phone num domain type
    }
    val numberInput = remember { mutableStateOf(TextFieldValue()) }

    Column {
        textEntryRow(numberInput, sourceNumbers)
        sourceNumberListView(sourceNumbers)
    }
}

private val rowModifier = Modifier
    .fillMaxWidth()
    .height(Dp(60f))

@Composable
private fun textEntryRow(
    numberInput: MutableState<TextFieldValue>,
    sourceNumbers: SnapshotStateList<String>
) {
    Row(rowModifier) { //todo these modifiers can /probably/ be "moved" to "stylesheet" (BudgetTheme)
        TextField(
            value = numberInput.value,
            onValueChange = {
                numberInput.value = it
            },

            modifier = Modifier.weight(0.8f),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = true,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            maxLines = 1,
            singleLine = true
        )

        Button(
            onClick = {
                sourceNumbers.add(numberInput.value.text)
                numberInput.value = TextFieldValue("")
            },
            modifier = Modifier
                .weight(0.2f)
                .fillMaxHeight()
        ) {
            Text("Add")
        }
    }
}

@Composable
private fun sourceNumberListView(sourceNumbers: SnapshotStateList<String>){
    LazyColumn {
        itemsIndexed(sourceNumbers){ index, item ->
            Row(rowModifier
                .wrapContentHeight(align = Alignment.CenterVertically)
                .combinedClickable(
                onLongClick = {
                    sourceNumbers.removeAt(index)
                },
                onClick = {})){
                Text(item)
            }
        }
    }
}
