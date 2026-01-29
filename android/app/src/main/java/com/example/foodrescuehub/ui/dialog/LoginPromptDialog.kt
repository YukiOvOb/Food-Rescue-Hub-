package com.example.foodrescuehub.ui.dialog

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.foodrescuehub.R

object LoginPromptDialog {

    /**
     * Show a dialog prompting the user to login
     *
     * @param context The context to show the dialog in
     * @param onLoginClicked Callback when user clicks "Login"
     */
    fun show(context: Context, onLoginClicked: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.login_required_title)
            .setMessage(R.string.login_required_message)
            .setPositiveButton(R.string.login_prompt_login) { dialog, _ ->
                dialog.dismiss()
                onLoginClicked()
            }
            .setNegativeButton(R.string.login_prompt_later) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
