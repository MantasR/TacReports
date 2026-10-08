package lt.tacreports.bubble

import kotlinx.coroutines.flow.MutableStateFlow

/** Shared between the fill screen and the bubble so the bubble hides while a report is open. */
object BubbleState {
    val formVisible = MutableStateFlow(false)
}
