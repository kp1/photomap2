package net.mmho.photomap2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView

internal object PermissionUtils {
    fun requestPermission(rootView: View?, c: Context) {
        if (rootView != null) {
            val snackbar = Snackbar.make(rootView, R.string.request_permission, Snackbar.LENGTH_LONG)
            val text = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView

            snackbar.setActionTextColor(c.resources.getColor(R.color.primary,null))
            text.setTextColor(c.resources.getColor(R.color.textPrimary, null))
            snackbar.setAction(R.string.setting) { v ->
                val uri = Uri.parse("package:" + c.applicationContext.packageName)
                val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                c.startActivity(i)
            }
            snackbar.show()
        }

    }
}