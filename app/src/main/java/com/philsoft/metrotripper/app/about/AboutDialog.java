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
package com.philsoft.metrotripper.app.about;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.philsoft.metrotripper.R;
import com.philsoft.metrotripper.utils.EZ;
import com.philsoft.metrotripper.utils.ui.Ui;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class AboutDialog extends DialogFragment {

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Get app version
		String versionName = EZ.INSTANCE.getAppVersion(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		ScrollView aboutLayout = (ScrollView) inflater.inflate(R.layout.dialog_about, null);

		TextView version = Ui.INSTANCE.findView(aboutLayout, R.id.dialog_about_version);
		version.setText(getString(R.string.about_version, versionName));

		TextView licensesLink = Ui.INSTANCE.findView(aboutLayout, R.id.dialog_about_licenses);
		licensesLink.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showOpenSourceLicenses(getActivity());
			}
		});

		TextView description = Ui.INSTANCE.findView(aboutLayout, R.id.dialog_about_description);
		try {
			InputStream inputStream = getResources().getAssets().open("description.html");
			String html = IOUtils.toString(inputStream);
			description.setText(Html.fromHtml(html));
		} catch (IOException exception) {
			description.setText("");
		}


		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(aboutLayout)
				.setInverseBackgroundForced(true)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						});
		AlertDialog dialog = builder.create();
		return dialog;
	}

	public static void showOpenSourceLicenses(Activity activity) {
		new OpenSourceLicensesDialog().show(activity.getFragmentManager(), "dialog_licenses");
	}
}
