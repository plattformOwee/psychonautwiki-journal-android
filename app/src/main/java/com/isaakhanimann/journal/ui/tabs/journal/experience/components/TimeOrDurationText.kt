/*
 * Copyright (c) 2024. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.journal.experience.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import com.isaakhanimann.journal.ui.utils.getStringOfPattern
import java.time.Instant

const val TIME_RANGE_SEPARATOR_TEXT = " - "

@Composable
fun TimeOrDurationText(
    time: Instant,
    endTime: Instant?,
    index: Int,
    timeDisplayOption: TimeDisplayOption,
    allTimesSortedMap: List<Instant>
) {
    val isFirstIngestion = index == 0
    val textStyle = MaterialTheme.typography.labelMedium
    when (timeDisplayOption) {
        TimeDisplayOption.REGULAR -> {
            RegularTimeOrRangeText(time, endTime, textStyle)
        }

        TimeDisplayOption.RELATIVE_TO_NOW -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeRelativeToNowText(
                    time = time,
                    style = textStyle
                )
                if (endTime != null) {
                    Text(TIME_RANGE_SEPARATOR_TEXT)
                    TimeRelativeToNowText(
                        time = endTime,
                        style = textStyle
                    )
                }
            }
        }

        TimeDisplayOption.TIME_BETWEEN -> {
            if (isFirstIngestion) {
                RegularTimeOrRangeText(time, endTime, textStyle)
            } else {
                val previousTime =
                    allTimesSortedMap[index - 1]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getDurationText(
                            fromInstant = previousTime,
                            toInstant = time
                        ) + " later",
                        style = textStyle
                    )
                    if (endTime != null) {
                        Text(TIME_RANGE_SEPARATOR_TEXT)
                        Text(
                            text = getDurationText(
                                fromInstant = previousTime,
                                toInstant = endTime
                            ) + " later",
                            style = textStyle
                        )
                    }
                }
            }
        }

        TimeDisplayOption.RELATIVE_TO_START -> {
            if (isFirstIngestion) {
                RegularTimeOrRangeText(time, endTime, textStyle)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val startTime = allTimesSortedMap.firstOrNull()
                        ?: Instant.now()
                    Text(
                        text = getDurationText(
                            fromInstant = startTime,
                            toInstant = time
                        ) + " since start",
                        style = textStyle
                    )
                    if (endTime != null) {
                        Text(TIME_RANGE_SEPARATOR_TEXT)
                        Text(
                            text = getDurationText(
                                fromInstant = startTime,
                                toInstant = endTime
                            ) + " since start",
                            style = textStyle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegularTimeOrRangeText(
    time: Instant,
    endTime: Instant?,
    textStyle: TextStyle
) {
    val startText = time.getStringOfPattern("EEE HH:mm")
    val text = if (endTime == null) {
        startText
    } else {
        val startDay = time.getStringOfPattern("EEE")
        val endDay = endTime.getStringOfPattern("EEE")
        if (startDay == endDay) {
            startText + TIME_RANGE_SEPARATOR_TEXT + endTime.getStringOfPattern("HH:mm")
        } else {
            startText + TIME_RANGE_SEPARATOR_TEXT + endTime.getStringOfPattern("EEE HH:mm")
        }
    }
    Text(
        text = text,
        style = textStyle
    )
}