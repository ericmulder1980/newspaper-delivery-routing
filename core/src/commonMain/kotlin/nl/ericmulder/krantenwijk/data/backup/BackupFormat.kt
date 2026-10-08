package nl.ericmulder.krantenwijk.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.BuildingSnapshot
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.RouteSnapshot
import nl.ericmulder.krantenwijk.domain.model.SectionSnapshot
import nl.ericmulder.krantenwijk.domain.model.Segment
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType

/**
 * The backup file (DATA-02, DEC-026): plain JSON with a format version, so later app versions
 * can still read older backups. Field names are part of the format: never rename, only add
 * (unknown fields are ignored when reading).
 */
object BackupFormat {
    /**
     * Bump when the format changes; [decode] keeps reading older versions.
     * 1: route only (DATA-A). 2: adds finished rounds (DEC-030); a version-1 file has none.
     * 3: adds a building's own addition, e.g. 8A (DEC-031). Older versions must refuse it: they
     * would drop the addition and merge 8A and 8B into one number.
     */
    const val VERSION = 3

    /** File type used by the file picker. */
    const val MIME_TYPE = "application/json"

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(snapshot: RouteSnapshot, exportedAtMillis: Long, appVersion: String): String =
        json.encodeToString(BackupFileV1.serializer(), snapshot.toFile(exportedAtMillis, appVersion))

    /** Reads a backup; throws [BackupException] with a [BackupException.Reason] the UI can explain. */
    fun decode(text: String): DecodedBackup {
        val file = try {
            json.decodeFromString(BackupFileV1.serializer(), text)
        } catch (e: SerializationException) {
            throw BackupException(BackupException.Reason.NOT_A_BACKUP, e)
        } catch (e: IllegalArgumentException) {
            throw BackupException(BackupException.Reason.NOT_A_BACKUP, e)
        }
        if (file.app != APP_ID) throw BackupException(BackupException.Reason.NOT_A_BACKUP)
        if (file.formatVersion > VERSION) throw BackupException(BackupException.Reason.TOO_NEW)
        val snapshot = try {
            file.toSnapshot()
        } catch (e: IllegalArgumentException) {
            throw BackupException(BackupException.Reason.DAMAGED, e)
        }
        return DecodedBackup(snapshot, file.exportedAtMillis, file.appVersion)
    }

    internal const val APP_ID = "nl.ericmulder.krantenwijk"
}

/** A backup that was read successfully, with when and by which app version it was made. */
data class DecodedBackup(val snapshot: RouteSnapshot, val exportedAtMillis: Long, val appVersion: String)

class BackupException(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason {
        /** Not JSON, or not a Krantenwijk backup. */
        NOT_A_BACKUP,

        /** Made by a newer app version with a format this version can't read. */
        TOO_NEW,

        /** Right format, but contents are invalid (e.g. a negative house number). */
        DAMAGED,
    }
}

@Serializable
private data class BackupFileV1(
    val app: String,
    val formatVersion: Int,
    val exportedAtMillis: Long,
    val appVersion: String,
    val route: RouteV1,
    /** Since format 2. */
    val rounds: List<RoundV2> = emptyList(),
)

@Serializable
private data class RoundV2(
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val newspapers: Int,
    val leaflets: Int,
)

@Serializable
private data class RouteV1(
    val name: String,
    val town: String? = null,
    val createdAtMillis: Long,
    /** In walking order. */
    val sections: List<SectionV1>,
)

@Serializable
private data class SectionV1(
    val streetName: String,
    val side: Side,
    val rangeFrom: Int,
    val rangeTo: Int,
    val direction: Direction,
    val addresses: List<AddressV1>,
    val buildings: List<BuildingV1> = emptyList(),
)

@Serializable
private data class BuildingV1(
    val houseNumber: Int,
    val name: String? = null,
    val suffixType: SuffixType,
    val separator: String,
    val apartments: List<AddressV1>,
    /** Since format 3; null for a plain number. */
    val addition: String? = null,
)

@Serializable
private data class AddressV1(
    val houseNumber: Int,
    val addition: String? = null,
    val exists: Boolean = true,
    val sticker: Sticker = Sticker.NONE,
    @SerialName("noNewspaper") val exceptionNoNewspaper: Boolean = false,
    @SerialName("noLeaflets") val exceptionNoLeaflets: Boolean = false,
    val note: String? = null,
)

private fun RouteSnapshot.toFile(exportedAtMillis: Long, appVersion: String) = BackupFileV1(
    app = BackupFormat.APP_ID,
    formatVersion = BackupFormat.VERSION,
    exportedAtMillis = exportedAtMillis,
    appVersion = appVersion,
    route = RouteV1(
        name = route.name,
        town = route.town,
        createdAtMillis = route.createdAtMillis,
        sections = sections.map { s ->
            SectionV1(
                streetName = s.segment.streetName,
                side = s.segment.side,
                rangeFrom = s.segment.rangeFrom,
                rangeTo = s.segment.rangeTo,
                direction = s.segment.direction,
                addresses = s.addresses.map { it.toV1() },
                buildings = s.buildings.map { b ->
                    BuildingV1(b.building.houseNumber, b.building.name, b.building.suffixType, b.building.separator, b.apartments.map { it.toV1() }, b.building.addition)
                },
            )
        },
    ),
    rounds = rounds.map { RoundV2(it.startedAtMillis, it.finishedAtMillis, it.newspapers, it.leaflets) },
)

private fun Address.toV1() = AddressV1(houseNumber, addition, exists, sticker, exceptionNoNewspaper, exceptionNoLeaflets, note)

private fun AddressV1.toDomain() = Address(
    houseNumber = houseNumber,
    addition = addition?.ifBlank { null },
    exists = exists,
    sticker = sticker,
    exceptionNoNewspaper = exceptionNoNewspaper,
    exceptionNoLeaflets = exceptionNoLeaflets,
    note = note,
)

/** Validates while converting; domain constructors reject e.g. non-positive house numbers. */
private fun BackupFileV1.toSnapshot(): RouteSnapshot {
    require(route.name.isNotBlank()) { "Route name is blank" }
    require(rounds.all { it.newspapers >= 0 && it.leaflets >= 0 }) { "A round has a negative count" }
    require(rounds.distinctBy { it.startedAtMillis }.size == rounds.size) { "Two rounds have the same start time" }
    return RouteSnapshot(
        route = Route(name = route.name, town = route.town, createdAtMillis = route.createdAtMillis),
        sections = route.sections.mapIndexed { index, s ->
            require(s.streetName.isNotBlank()) { "Section ${index + 1} has no street name" }
            SectionSnapshot(
                segment = Segment(s.streetName, s.side, s.rangeFrom, s.rangeTo, s.direction, position = index),
                addresses = s.addresses.map { it.toDomain() },
                buildings = s.buildings.map { b ->
                    BuildingSnapshot(
                        Building(
                            segmentId = 0,
                            houseNumber = b.houseNumber,
                            suffixType = b.suffixType,
                            separator = b.separator,
                            name = b.name,
                            addition = b.addition?.ifBlank { null },
                        ),
                        b.apartments.map { it.toDomain() },
                    )
                },
            )
        },
        rounds = rounds.map { CompletedRound(id = 0, it.startedAtMillis, it.finishedAtMillis, it.newspapers, it.leaflets) },
    )
}
