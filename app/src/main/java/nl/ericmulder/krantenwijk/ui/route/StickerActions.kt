package nl.ericmulder.krantenwijk.ui.route

import nl.ericmulder.krantenwijk.domain.repository.RouteRepository

/**
 * Applies a sticker-sheet choice to addresses, the same way on every screen: a sticker also marks
 * them as existing again; "does not exist" keeps the sticker so it comes back when restored.
 */
suspend fun RouteRepository.applyStickerOption(option: StickerOption, addressIds: Collection<Long>) {
    if (addressIds.isEmpty()) return
    when (option) {
        is StickerOption.Set -> {
            setSticker(addressIds, option.sticker)
            setExists(addressIds, true)
        }
        StickerOption.DoesNotExist -> setExists(addressIds, false)
    }
}
