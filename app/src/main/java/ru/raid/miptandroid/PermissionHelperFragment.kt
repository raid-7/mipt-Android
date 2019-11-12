package ru.raid.miptandroid

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

typealias PermissionCallback = (Boolean) -> Unit

open class PermissionHelperFragment : Fragment() {
    private var code: Int = 0
    private val requests = mutableMapOf<Int, Request>()

    fun withPermissions(
        permissions: Array<out String>,
        rationale: Int? = null,
        rationaleSettings: Int? = null,
        callback: PermissionCallback
    ) {
        val context = checkNotNull(context) { "No context" }

        val notYetGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }.toTypedArray()
        if (notYetGranted.isEmpty()) {
            callback(true)
            return
        }

        val requestCode = code++
        requests[requestCode] = Request(callback, rationale, rationaleSettings)
        requestPermissions(notYetGranted, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val request = requests.remove(requestCode)
            ?: return super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        if (granted) {
            request.callback(true)
        } else {
            val activateInSettings = permissions.any { !shouldShowRequestPermissionRationale(it) }
            if (request.rationale != null && !activateInSettings) {
                Toast.makeText(context, request.rationale, Toast.LENGTH_SHORT).show()
            }
            if (request.rationaleSettings != null && activateInSettings) {
                Toast.makeText(context, request.rationaleSettings, Toast.LENGTH_SHORT).show()
            }

            request.callback(false)
        }
    }

    private class Request(
        val callback: PermissionCallback,
        val rationale: Int?,
        val rationaleSettings: Int?
    )
}
