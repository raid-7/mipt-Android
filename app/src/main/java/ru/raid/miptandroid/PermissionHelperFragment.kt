package ru.raid.miptandroid

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

abstract class PermissionHelperFragment<Tag> : Fragment() {
    private var code: Int = 0
    private val requests = mutableMapOf<Int, Request<Tag>>()

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val request = requests.remove(requestCode)
            ?: return super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            onPermissionsResult(request.tag, true)
        } else {
            val activateInSettings = permissions.any { !shouldShowRequestPermissionRationale(it) }
            if (request.rationale != null && !activateInSettings) {
                Toast.makeText(context, request.rationale, Toast.LENGTH_SHORT).show()
            }
            if (request.rationaleSettings != null && activateInSettings) {
                Toast.makeText(context, request.rationaleSettings, Toast.LENGTH_SHORT).show()
            }

            onPermissionsResult(request.tag, false)
        }
    }

    protected fun withPermissions(
        permissions: Array<out String>,
        rationale: Int? = null,
        rationaleSettings: Int? = null,
        tag: Tag
    ) {
        val context = checkNotNull(context) { "No context" }

        val notYetGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()
        if (notYetGranted.isEmpty()) {
            onPermissionsResult(tag, true)
            return
        }

        val requestCode = code++
        requests[requestCode] = Request(tag, rationale, rationaleSettings)
        requestPermissions(notYetGranted, requestCode)
    }

    protected abstract fun onPermissionsResult(tag: Tag, granted: Boolean)

    private class Request<Tag>(
        val tag: Tag,
        val rationale: Int?,
        val rationaleSettings: Int?
    )
}
