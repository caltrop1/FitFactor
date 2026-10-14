package com.sami.setra.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sami.setra.data.model.ExerciseProgressionPoint
import com.sami.setra.ui.theme.Dimens
import java.util.Locale

@Composable
fun SetraLineChart(
    points: List<ExerciseProgressionPoint>,
    valueSelector: (ExerciseProgressionPoint) -> Double,
    unit: String,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No progression data recorded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val values = points.map { valueSelector(it) }
    val minY = (values.minOrNull() ?: 0.0) * 0.9
    val maxY = (values.maxOrNull() ?: 100.0) * 1.1
    val rangeY = if (maxY - minY == 0.0) 1.0 else maxY - minY

    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected Point Info Header
        val selectedPoint = selectedIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()
        val selectedValue = selectedPoint?.let { valueSelector(it) } ?: 0.0

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedValue % 1.0 == 0.0) "${selectedValue.toInt()} $unit" else String.format(Locale.getDefault(), "%.1f %s", selectedValue, unit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(Dimens.spaceSmall))
            selectedPoint?.let { pt ->
                Text(
                    text = "• ${pt.formattedDate} (${pt.workoutName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val width = size.width.toFloat()
                            val stepX: Float = if (points.size > 1) width / (points.size - 1) else width / 2f
                            val closestIndex = ((tapOffset.x + (stepX / 2f)) / stepX).toInt().coerceIn(0, points.size - 1)
                            selectedIndex = closestIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                val paddingY = 24.dp.toPx()
                val usableHeight = height - (paddingY * 2)

                // Draw Horizontal Grid Lines (3 lines)
                val gridLines = 3
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                for (i in 0 until gridLines) {
                    val y = paddingY + (usableHeight * i / (gridLines - 1))
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )
                }

                if (points.size == 1) {
                    // Single point rendering
                    val pointY = paddingY + (usableHeight / 2f)
                    val pointX = width / 2f
                    drawCircle(
                        color = lineColor,
                        radius = 8.dp.toPx(),
                        center = Offset(pointX, pointY)
                    )
                } else {
                    val stepX = width / (points.size - 1)
                    val path = Path()
                    val fillPath = Path()

                    val coordinates = points.mapIndexed { index, point ->
                        val value = valueSelector(point)
                        val normY = (value - minY) / rangeY
                        val x = index * stepX
                        val y = height - paddingY - (normY * usableHeight).toFloat()
                        Offset(x, y)
                    }

                    // Build line and area path
                    path.moveTo(coordinates[0].x, coordinates[0].y)
                    fillPath.moveTo(coordinates[0].x, height)
                    fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                    for (i in 1 until coordinates.size) {
                        val prev = coordinates[i - 1]
                        val current = coordinates[i]
                        val controlX1 = prev.x + (current.x - prev.x) / 2f
                        val controlY1 = prev.y
                        val controlX2 = prev.x + (current.x - prev.x) / 2f
                        val controlY2 = current.y

                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, current.x, current.y)
                    }

                    fillPath.lineTo(coordinates.last().x, height)
                    fillPath.close()

                    // Draw Gradient Fill under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.35f),
                                lineColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw Line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw Data Dots
                    coordinates.forEachIndexed { index, offset ->
                        val isSelected = index == (selectedIndex ?: (points.size - 1))
                        drawCircle(
                            color = if (isSelected) surfaceColor else lineColor,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = lineColor,
                            radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                            center = offset,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }

        // X-Axis Start & End Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.spaceXSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = points.first().formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            if (points.size > 1) {
                Text(
                    text = points.last().formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
