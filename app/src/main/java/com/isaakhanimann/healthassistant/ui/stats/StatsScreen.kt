package com.isaakhanimann.healthassistant.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaakhanimann.healthassistant.ui.search.substance.roa.toReadableString


@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    navigateToSubstanceCompanion: (substanceName: String) -> Unit
) {
    StatsScreen(
        navigateToSubstanceCompanion = navigateToSubstanceCompanion,
        onTapOption = viewModel::onTapOption,
        statsModel = viewModel.statsModelFlow.collectAsState().value
    )
}

@Preview
@Composable
fun StatsPreview(
    @PreviewParameter(
        StatsPreviewProvider::class,
    ) statsModel: StatsModel
) {
    StatsScreen(
        navigateToSubstanceCompanion = {},
        onTapOption = {},
        statsModel = statsModel
    )
}

@Composable
fun StatsScreen(
    navigateToSubstanceCompanion: (substanceName: String) -> Unit,
    onTapOption: (option: TimePickerOption) -> Unit,
    statsModel: StatsModel?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ingestion Statistics") },
                elevation = 0.dp
            )
        }
    ) {
        if (statsModel?.substanceStats?.isEmpty() != false) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(text = "Nothing to Show Yet")
                }
            }
        } else {
            Column {
                TabRow(selectedTabIndex = statsModel.selectedOption.tabIndex) {
                    TimePickerOption.values().forEachIndexed { index, option ->
                        Tab(
                            text = { Text(option.displayText) },
                            selected = statsModel.selectedOption.tabIndex == index,
                            onClick = { onTapOption(option) }
                        )
                    }
                }
                val isDarkTheme = isSystemInDarkTheme()
                val chartDividerColor = MaterialTheme.colors.onSurface.copy(alpha = 0.20f)
                val buckets = statsModel.chartBuckets
                Canvas(
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeightOuter = size.height
                    val tickHeight = 8f
                    val numBuckets = buckets.size
                    val numSpacers = numBuckets + 1
                    val spaceWidth = 20f
                    val bucketWidth = (canvasWidth - (numBuckets * spaceWidth)) / numBuckets
                    inset(left = 0f, top = 0f, right = 0f, bottom = tickHeight) {
                        val canvasHeightInner = size.height
                        val maxCount = buckets.maxOf { bucket ->
                            bucket.sumOf { it.count }
                        }
                        buckets.forEachIndexed { index, colorCounts ->
                            val xBucket = (((index * 2f) + 1) / 2) * (spaceWidth + bucketWidth)
                            var yStart = canvasHeightInner
                            colorCounts.forEach { colorCount ->
                                val yLength = colorCount.count * canvasHeightInner / maxCount
                                val yEnd = yStart - yLength
                                drawLine(
                                    color = colorCount.color.getComposeColor(isDarkTheme),
                                    start = Offset(x = xBucket, y = yStart),
                                    end = Offset(x = xBucket, y = yEnd),
                                    strokeWidth = bucketWidth,
                                )
                                yStart = yEnd
                            }
                        }
                    }
                    // bottom line
                    val bottomLineWidth = 4f
                    drawLine(
                        color = chartDividerColor,
                        start = Offset(x = 0f, y = canvasHeightOuter - tickHeight),
                        end = Offset(x = canvasWidth, y = canvasHeightOuter - tickHeight),
                        strokeWidth = bottomLineWidth,
                        cap = StrokeCap.Round
                    )
                    for (index in 0 until numSpacers) {
                        val xSpacer = index * (canvasWidth / numBuckets)
                        drawLine(
                            color = chartDividerColor,
                            start = Offset(x = xSpacer, y = canvasHeightOuter),
                            end = Offset(x = xSpacer, y = canvasHeightOuter - tickHeight),
                            strokeWidth = bottomLineWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 5.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val style = MaterialTheme.typography.subtitle2
                    Text(
                        text = statsModel.startDateText,
                        style = style,
                    )
                    ArrowRight(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "Now",
                        style = style,
                    )
                }
                Divider()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(statsModel.substanceStats.size) { i ->
                        val subStat = statsModel.substanceStats[i]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navigateToSubstanceCompanion(subStat.substanceName)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = subStat.color.getComposeColor(isDarkTheme),
                                modifier = Modifier.size(25.dp)
                            ) {}
                            Column {
                                Text(
                                    text = subStat.substanceName,
                                    style = MaterialTheme.typography.h6
                                )
                                val cumulativeDose = subStat.cumulativeDose
                                if (cumulativeDose != null) {
                                    Text(text = "Cumulative dose: ${if (cumulativeDose.isEstimate) "~" else ""} ${cumulativeDose.dose.toReadableString()} ${cumulativeDose.units}")
                                } else {
                                    Text(text = "Cumulative dose unknown")
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Column {
                                Text(
                                    text = subStat.ingestionCount.toString() + if (subStat.ingestionCount == 1) " Ingestion" else " Ingestions",
                                    style = MaterialTheme.typography.subtitle2
                                )
                                subStat.routeCounts.forEach {
                                    Text(text = "${it.count}x ${it.administrationRoute.displayText}")
                                }
                            }

                        }
                        if (i < statsModel.substanceStats.size) {
                            Divider()
                        }
                    }
                }
            }


        }
    }
}