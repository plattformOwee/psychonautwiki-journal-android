/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
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

package com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.drawables.timelines

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.WeightedLine
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.drawables.TimelineDrawable
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.ingestionDotRadius
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.normalStroke
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.shapeAlpha
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.strokeWidth
import java.time.Duration
import java.time.Instant
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class FullTimelines(
    val onset: FullDurationRange,
    val comeup: FullDurationRange,
    val peak: FullDurationRange,
    val offset: FullDurationRange,
    val weightedLines: List<WeightedLine>,
    val startTimeGraph: Instant,
) : TimelineDrawable {

    data class WeightedLineRelativeToFirst(
        val startTimeRelativeToGroupInSeconds: Float,
        val horizontalWeight: Float,
        val height: Float
    )

    data class LineSegment(
        val start: Point,
        val end: Point
    ) {
        fun isInside(x: Float): Boolean {
            return start.x <= x && x < end.x
        }

        fun heightAt(x: Float): Float {
            val divider = end.x - start.x
            if (divider == 0f) return 0f
            val m = (end.y - start.y) / divider
            val b = start.y - m * start.x
            return m * x + b
        }
    }

    data class Point(
        val x: Float,
        val y: Float,
    )

    data class FinalPoint(
        val x: Float,
        val y: Float,
        val isIngestionPoint: Boolean = false
    )

    private val finalPoints: List<FinalPoint>

    override var referenceHeight: Float = 1f

    override val nonNormalisedHeight: Float

    override val endOfLineRelativeToStartInSeconds: Float

    init {
        val timeRangeLineSegments = weightedLines.flatMap {
            if (it.endTime == null) {
                return@flatMap emptyList<LineSegment>()
            }

            val startX =
                Duration.between(startTimeGraph, it.startTime).seconds.toFloat()
            val endX =
                Duration.between(startTimeGraph, it.endTime).seconds.toFloat()
            val onsetInSeconds = onset.interpolateAtValueInSeconds(0.5f)
            val comeupInSeconds = comeup.interpolateAtValueInSeconds(0.5f)
            val peakInSeconds = peak.interpolateAtValueInSeconds(0.5f)
            val offsetInSeconds = offset.interpolateAtValueInSeconds(0.5f)

            val points = getSamplePoints(
                xStart = startX.toDouble(),
                xEnd = endX.toDouble(),
                hMax = it.height.toDouble(),
                onset = onsetInSeconds.toDouble(),
                comeup = comeupInSeconds.toDouble(),
                peak = peakInSeconds.toDouble(),
                offset = offsetInSeconds.toDouble()
            )
            val lineSegments = getLineSegments(points)

            return@flatMap lineSegments
        }
        val weightedRelatives = weightedLines.filter { it.endTime == null }.map {
            WeightedLineRelativeToFirst(
                startTimeRelativeToGroupInSeconds = Duration.between(
                    startTimeGraph,
                    it.startTime
                ).seconds.toFloat(),
                horizontalWeight = it.horizontalWeight,
                height = it.height
            )
        }
        val lineSegments = weightedRelatives.flatMap { relative ->
            val result = mutableListOf<LineSegment>()
            val onsetAndComeupWeight = 0.5f
            val onsetEndX =
                relative.startTimeRelativeToGroupInSeconds + onset.interpolateAtValueInSeconds(
                    onsetAndComeupWeight
                )
            val comeupStartPoint = Point(x = onsetEndX, y = 0f)
            val comeupEndX =
                onsetEndX + comeup.interpolateAtValueInSeconds(onsetAndComeupWeight)
            val peakStartPoint = Point(x = comeupEndX, y = relative.height)
            result.add(
                LineSegment(
                    start = comeupStartPoint,
                    end = peakStartPoint
                )
            )
            val peakEndX =
                comeupEndX + (peak.interpolateAtValueInSeconds(relative.horizontalWeight))
            val peakEndPoint = Point(x = peakEndX, y = relative.height)
            result.add(
                LineSegment(
                    start = peakStartPoint,
                    end = peakEndPoint
                )
            )
            val offsetEndX =
                peakEndX + offset.interpolateAtValueInSeconds(relative.horizontalWeight)
            val offsetEndPoint = Point(x = offsetEndX, y = 0f)
            result.add(
                LineSegment(
                    start = peakEndPoint,
                    end = offsetEndPoint
                )
            )
            return@flatMap result
        } + timeRangeLineSegments
        val linePoints = lineSegments.flatMap { lineSegment ->
            listOf(lineSegment.start.x, lineSegment.end.x)
        }.distinct().map { FinalPoint(x = it, y = 0f) }
        val ingestionPoints = weightedRelatives.map {
            FinalPoint(
                x = it.startTimeRelativeToGroupInSeconds,
                y = 0f,
                isIngestionPoint = true
            )
        }
        val pointsToConsider = ingestionPoints + linePoints
        val pointsWithHeight = pointsToConsider.map { finalPoint ->
            val x = finalPoint.x
            val sumOfHeights = lineSegments.map { lineSegment ->
                if (lineSegment.isInside(x)) {
                    lineSegment.heightAt(x)
                } else {
                    0f
                }
            }.sum()
            return@map FinalPoint(
                x = x,
                y = sumOfHeights,
                isIngestionPoint = finalPoint.isIngestionPoint
            )
        }
        val sortedPoints = pointsWithHeight.sortedBy { it.x }
        this.finalPoints = sortedPoints
        this.nonNormalisedHeight = sortedPoints.maxOf { it.y }
        this.endOfLineRelativeToStartInSeconds = finalPoints.maxOf { it.x }
    }

    companion object {

        fun getLineSegments(points: List<Point>): List<LineSegment> {
            if (points.isEmpty()) {
                return emptyList()
            }
            val result: MutableList<LineSegment> = mutableListOf()
            var previousPoint = points.first()
            for (currentPoint in points.takeLast(points.size - 1)) {
                result.add(
                    LineSegment(
                        start = previousPoint,
                        end = currentPoint
                    )
                )
                previousPoint = currentPoint
            }
            return result
        }

        fun getSamplePoints(
            xStart: Double,
            xEnd: Double,
            hMax: Double,
            onset: Double,
            peak: Double,
            comeup: Double,
            offset: Double,
        ): List<Point> {
            val numberOfSteps = 20
            val startSampleRange = xStart + onset
            val endSampleRange = xEnd + onset + comeup + peak + offset
            val stepSize = (endSampleRange - startSampleRange) / numberOfSteps

            val points = (1..<numberOfSteps).map { step ->
                val t = step * stepSize
                val height = calculateExpression(
                    t = t,
                    xStart = xStart,
                    xEnd = xEnd,
                    hMax = hMax,
                    onset = onset,
                    peak = peak,
                    comeup = comeup,
                    offset = offset
                )
                return@map Point(x = t.toFloat(), y = height.toFloat())
            }
            val firstPoint = Point(x = startSampleRange.toFloat(), y = 0f)
            val lastPoint = Point(x = endSampleRange.toFloat(), y = 0f)

            return listOf(firstPoint) + points + listOf(lastPoint)
        }

        private fun calculateExpression(
            t: Double,
            xStart: Double,
            xEnd: Double,
            hMax: Double,
            onset: Double,
            peak: Double,
            comeup: Double,
            offset: Double,
        ): Double {
            val term1 = comeup * offset * (
                    min(xEnd, max(xStart, -comeup - onset + t)) -
                            min(xEnd, max(xStart, -comeup - onset - peak + t))
                    )

            val term2 = comeup * (
                    min(xEnd, max(xStart, -comeup - onset - peak + t)) -
                            min(xEnd, max(xStart, -comeup - offset - onset - peak + t))
                    ) * (comeup + offset + onset + peak - t)

            val term3 = 0.5 * comeup * (
                    min(xEnd, max(xStart, -comeup - onset - peak + t)).pow(2) -
                            min(xEnd, max(xStart, -comeup - offset - onset - peak + t)).pow(2)
                    )

            val term4 = offset * (onset - t) * (
                    -min(xEnd, max(xStart, -onset + t)) +
                            min(xEnd, max(xStart, -comeup - onset + t))
                    )

            val term5 = 0.5 * offset * (
                    -min(xEnd, max(xStart, -onset + t)).pow(2) +
                            min(xEnd, max(xStart, -comeup - onset + t)).pow(2)
                    )

            val numerator = hMax.pow(2) * (term1 + term2 + term3 + term4 + term5)
            val denominator = comeup * offset * (xEnd - xStart)

            return numerator / denominator
        }
    }

    override fun drawTimeLine(
        drawScope: DrawScope,
        canvasHeight: Float,
        pixelsPerSec: Float,
        color: Color,
        density: Density
    ) {
        val finalPointsNormalised = finalPoints.map {
            FinalPoint(
                x = it.x,
                y = it.y / referenceHeight,
                isIngestionPoint = it.isIngestionPoint
            )
        }
        val path = Path().apply {
            val firstPoint = finalPointsNormalised.first()
            val rest = finalPointsNormalised.drop(1)
            val firstHeightInPx = firstPoint.y * canvasHeight
            moveTo(x = firstPoint.x * pixelsPerSec, y = canvasHeight - firstHeightInPx)
            for (point in rest) {
                val heightInPx = point.y * canvasHeight
                lineTo(x = point.x * pixelsPerSec, y = canvasHeight - heightInPx)
            }
        }
        drawScope.drawPath(
            path = path,
            color = color,
            style = density.normalStroke
        )
        path.lineTo(
            x = finalPointsNormalised.last().x * pixelsPerSec,
            y = canvasHeight + drawScope.strokeWidth / 2
        )
        path.lineTo(
            x = finalPointsNormalised.first().x * pixelsPerSec,
            y = canvasHeight + drawScope.strokeWidth / 2
        )
        path.close()
        drawScope.drawPath(
            path = path,
            color = color.copy(alpha = shapeAlpha)
        )
        for (point in finalPointsNormalised) {
            if (point.isIngestionPoint) {
                drawScope.drawCircle(
                    color = color,
                    radius = density.ingestionDotRadius,
                    center = Offset(
                        x = point.x * pixelsPerSec,
                        y = canvasHeight - point.y * canvasHeight
                    )
                )
            }
        }
    }
}

fun RoaDuration.toFullTimelines(
    weightedLines: List<WeightedLine>,
    startTimeGraph: Instant,
): FullTimelines? {
    val fullOnset = onset?.toFullDurationRange()
    val fullComeup = comeup?.toFullDurationRange()
    val fullPeak = peak?.toFullDurationRange()
    val fullOffset = offset?.toFullDurationRange()
    return if (fullOnset != null && fullComeup != null && fullPeak != null && fullOffset != null) {
        FullTimelines(
            onset = fullOnset,
            comeup = fullComeup,
            peak = fullPeak,
            offset = fullOffset,
            weightedLines = weightedLines,
            startTimeGraph = startTimeGraph,
        )
    } else {
        null
    }

}