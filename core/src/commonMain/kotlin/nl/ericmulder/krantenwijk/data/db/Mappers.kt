package nl.ericmulder.krantenwijk.data.db

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.domain.model.Route
import nl.ericmulder.krantenwijk.domain.model.Segment

internal fun RouteEntity.toDomain() = Route(name = name, town = town, createdAtMillis = createdAtMillis, id = id)

internal fun SegmentEntity.toDomain() = Segment(
    streetName = streetName,
    side = side,
    rangeFrom = rangeFrom,
    rangeTo = rangeTo,
    direction = direction,
    position = position,
    id = id,
)

internal fun BuildingEntity.toDomain() = Building(
    segmentId = segmentId,
    houseNumber = houseNumber,
    suffixType = suffixType,
    separator = separator,
    name = name,
    id = id,
)

internal fun AddressEntity.toDomain() = Address(
    houseNumber = houseNumber,
    addition = addition.ifEmpty { null },
    exists = exists,
    sticker = sticker,
    exceptionNoNewspaper = exceptionNoNewspaper,
    exceptionNoLeaflets = exceptionNoLeaflets,
    note = note,
    id = id,
    segmentId = segmentId,
    buildingId = buildingId,
)

internal fun Address.toEntity() = AddressEntity(
    id = id,
    segmentId = segmentId,
    buildingId = buildingId,
    houseNumber = houseNumber,
    addition = addition.orEmpty(),
    exists = exists,
    sticker = sticker,
    exceptionNoNewspaper = exceptionNoNewspaper,
    exceptionNoLeaflets = exceptionNoLeaflets,
    note = note,
)

internal fun CompletedRoundEntity.toDomain() = CompletedRound(
    id = id,
    startedAtMillis = startedAtMillis,
    finishedAtMillis = finishedAtMillis,
    newspapers = newspapers,
    leaflets = leaflets,
)
