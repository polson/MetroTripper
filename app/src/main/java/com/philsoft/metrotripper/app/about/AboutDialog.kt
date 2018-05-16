/*
 * Based on code in HelpUtils.java
 * 
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.philsoft.metrotripper.app.about

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.webkit.WebView
import android.widget.ScrollView
import com.philsoft.metrotripper.R
import com.philsoft.metrotripper.utils.EZ
import kotlinx.android.synthetic.main.dialog_about.view.*
import org.apache.commons.io.IOUtils
import java.io.IOException


class AboutDialog {

    companion object {
        fun show(activity: Activity) {
            val versionName = EZ.getAppVersion(activity)
            val aboutLayout = activity.layoutInflater.inflate(R.layout.dialog_about, null) as ScrollView
            aboutLayout.versionNumber.text = activity.getString(R.string.about_version, versionName)
            aboutLayout.licenses.setOnClickListener { showOpenSourceLicenses(activity) }
            try {
                val inputStream = activity.resources.assets.open("description.html")
                val html = IOUtils.toString(inputStream)
                aboutLayout.description.text = fromHtml(html)
            } catch (exception: IOException) {
                aboutLayout.description.text = ""
            }

            AlertDialog.Builder(activity)
                    .setInverseBackgroundForced(true)
                    .setView(aboutLayout)
                    .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .create().show()
        }

        private fun showOpenSourceLicenses(activity: Activity) {
            val webView = WebView(activity)
            webView.loadUrl("file:///android_asset/licenses.html")

            AlertDialog.Builder(activity)
                    .setInverseBackgroundForced(true)
                    .setTitle(R.string.licenses)
                    .setView(webView)
                    .setPositiveButton(R.string.ok) { dialog, whichButton -> dialog.dismiss() }
                    .create()
                    .show()
        }

        private fun fromHtml(html: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
        }
    }
}
