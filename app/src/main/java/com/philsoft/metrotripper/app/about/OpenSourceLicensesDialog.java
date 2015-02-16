package com.philsoft.metrotripper.app.about;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;

import com.philsoft.metrotripper.R;


public class OpenSourceLicensesDialog extends DialogFragment {

	public OpenSourceLicensesDialog() {
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		WebView webView = new WebView(getActivity());
		webView.loadUrl("file:///android_asset/licenses.html");

		return new AlertDialog.Builder(getActivity())
				.setInverseBackgroundForced(true)
				.setTitle(R.string.licenses)
				.setView(webView)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}
				)
				.create();
	}
}