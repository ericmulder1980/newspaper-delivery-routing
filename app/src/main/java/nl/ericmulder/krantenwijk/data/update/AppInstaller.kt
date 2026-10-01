package nl.ericmulder.krantenwijk.data.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Result of handing an APK to Android's installer. On success the app is replaced and restarted. */
sealed interface InstallEvent {
    data object None : InstallEvent

    /** Android shows its own confirmation screen. */
    data object WaitingForUser : InstallEvent

    data class Failed(val message: String?) : InstallEvent
}

/** Installs a downloaded update over the running app (REL-C). */
interface AppInstaller {
    val events: StateFlow<InstallEvent>

    /** Whether the user has allowed "install unknown apps" for Krantenwijk. */
    fun canInstall(): Boolean

    /** Opens the system setting where the user allows installing updates. */
    fun permissionSettingsIntent(): Intent

    suspend fun install(apk: File)
}

@Singleton
class PackageInstallerAppInstaller @Inject constructor(@ApplicationContext private val context: Context) : AppInstaller {

    override val events: StateFlow<InstallEvent> = InstallEvents.state

    override fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    override fun permissionSettingsIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    override suspend fun install(apk: File) = withContext(Dispatchers.IO) {
        InstallEvents.state.value = InstallEvent.None
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(apk.length())
            // Self-updates may skip the extra confirmation (API 31+, with UPDATE_PACKAGES_WITHOUT_USER_ACTION).
            setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.openWrite("base.apk", 0, apk.length()).use { out ->
                apk.inputStream().use { it.copyTo(out) }
                session.fsync(out)
            }
            val callback = Intent(context, InstallResultReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                sessionId,
                callback,
                // Mutable: the system adds the status extras. The intent is explicit, as required.
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            session.commit(pending.intentSender)
        }
    }
}

/** Shared between the installer callback and the UI. */
internal object InstallEvents {
    val state = MutableStateFlow<InstallEvent>(InstallEvent.None)
}

/** Receives the installer's result; shows Android's confirmation screen when it asks for one. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                InstallEvents.state.value = InstallEvent.WaitingForUser
                IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)?.let {
                    context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            // On success the app is replaced and restarted; nothing to show.
            PackageInstaller.STATUS_SUCCESS -> InstallEvents.state.value = InstallEvent.None
            else -> InstallEvents.state.value =
                InstallEvent.Failed(intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE))
        }
    }
}
