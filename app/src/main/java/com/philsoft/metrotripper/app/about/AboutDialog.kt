package com.philsoft.metrotripper.app.about

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkAnnotation.Url
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.utils.EZ

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    var showLicenses by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionName = EZ.getAppVersion(context)

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text(text = stringResource(R.string.licenses)) },
            text = {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            loadUrl("file:///android_asset/licenses.html")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicenses = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.about_version, versionName),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                val annotatedString = buildAnnotatedString {
                    append("Copyright 2018 Phil Olson\n\n")
                    append("You can find me on GitHub at ")
                    withLink(Url("https://github.com/polson")) {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("https://github.com/polson")
                        }
                    }
                    append("\n\nTrip data provided by ")
                    withLink(Url("http://www.metrotransit.org/")) {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("Metro Transit")
                        }
                    }
                    append(" NexTrip API")
                }

                Text(
                    text = annotatedString,
                    style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.licenses),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { showLicenses = true }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
