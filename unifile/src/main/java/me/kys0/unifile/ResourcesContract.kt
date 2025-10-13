package me.kys0.unifile

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.text.TextUtils

internal object ResourcesContract {

    @SuppressLint("DiscouragedApi")
    fun openResource(context: Context, uri: Uri): OpenResourceResult? {
        val authority = uri.authority
        val r: Resources = if (TextUtils.isEmpty(authority)) return null else try {
            context.packageManager.getResourcesForApplication(authority!!)
        } catch (ex: PackageManager.NameNotFoundException) {
            return null
        }
        val path = uri.pathSegments ?: return null
        val len = path.size
        val id: Int
        val name: String?
        when (len) {
            1 -> {
                id = try {
                    path[0].toInt()
                } catch (e: NumberFormatException) {
                    return null
                }
                name = try {
                    r.getResourceEntryName(id)
                } catch (e: Resources.NotFoundException) {
                    return null
                }
            }

            2 -> {
                name = path[1]
                id = r.getIdentifier(path[1], path[0], authority)
            }

            else -> {
                return null
            }
        }
        if (id == 0 || name == null) {
            return null
        }
        val res = OpenResourceResult()
        res.r = r
        res.p = authority
        res.id = id
        res.name = name
        return res
    }

    class OpenResourceResult {
        var r: Resources? = null
        var p: String? = null
        var id = 0
        var name: String? = null
    }
}
