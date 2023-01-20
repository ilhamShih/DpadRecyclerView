/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear

import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutResult
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.PreLayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.StructureEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import kotlin.math.max

/**
 * General layout algorithm:
 * 1. Layout the view at the selected position (pivot) and align it to the keyline
 * 2. Starting from the bottom/end of the selected view,
 * fill towards the top/start until there's no more space
 * 3. Starting from the top/end of the selected view,
 * fill towards the top/start until there's no more space
 */
internal class LinearLayoutEngineer(
    layoutManager: LayoutManager,
    layoutInfo: LayoutInfo,
    layoutAlignment: LayoutAlignment,
    private val onChildLayoutListener: OnChildLayoutListener,
) : StructureEngineer(layoutManager, layoutInfo, layoutAlignment) {

    companion object {
        const val TAG = "LinearLayoutEngineer"
    }

    private val architect = LinearLayoutArchitect(layoutInfo)

    override fun updateLayoutRequestForScroll(
        layoutRequest: LayoutRequest,
        state: State,
        scrollOffset: Int
    ) {
        architect.updateLayoutStateForScroll(layoutRequest, state, scrollOffset)
    }

    override fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: RecyclerView.Recycler,
        state: State
    ): View {
        val pivotView = addPivot(pivotPosition, recycler)
        updatePivotBounds(pivotView, viewBounds, layoutRequest)
        layoutPivot(pivotView, viewBounds)
        layoutFromPivotToStart(pivotView, pivotPosition, layoutRequest, recycler, state)
        layoutFromPivotToEnd(pivotView, pivotPosition, layoutRequest, recycler, state)
        return pivotView
    }

    override fun preLayout(
        preLayoutRequest: PreLayoutRequest,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        val extraLayoutSpace = max(0, preLayoutRequest.endOffset - preLayoutRequest.startOffset)
        val firstView = preLayoutRequest.firstView
        if (firstView != null) {
            layoutRequest.prepend(preLayoutRequest.firstPosition) {
                setRecyclingEnabled(false)
                setCheckpoint(layoutInfo.getDecoratedStart(firstView))
                setAvailableScrollSpace(
                    architect.calculateAvailableScrollSpaceStart(extraLayoutSpace)
                )
                setFillSpace(extraLayoutSpace)
            }
            fill(layoutRequest, recycler, state)
        }

        val lastView = preLayoutRequest.lastView
        if (lastView != null) {
            layoutRequest.append(preLayoutRequest.lastPosition) {
                setRecyclingEnabled(false)
                setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
                setAvailableScrollSpace(
                    architect.calculateAvailableScrollSpaceEnd(extraLayoutSpace)
                )
                setFillSpace(extraLayoutSpace)
            }
            fill(layoutRequest, recycler, state)
        }
    }

    override fun predictiveLayout(
        firstView: View,
        lastView: View,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        val firstViewPosition = layoutInfo.getLayoutPositionOf(firstView)
        val scrapList = recycler.scrapList
        var scrapExtraStart = 0
        var scrapExtraEnd = 0
        for (i in 0 until scrapList.size) {
            val scrap = scrapList[i]
            if (layoutInfo.isRemoved(scrap)) {
                continue
            }
            val position = scrap.layoutPosition
            val direction = if (position < firstViewPosition != layoutRequest.reverseLayout) {
                LayoutRequest.LayoutDirection.START
            } else {
                LayoutRequest.LayoutDirection.END
            }
            if (direction == LayoutRequest.LayoutDirection.START) {
                scrapExtraStart += layoutInfo.getDecoratedSize(scrap.itemView)
            } else {
                scrapExtraEnd += layoutInfo.getDecoratedSize(scrap.itemView)
            }
        }

        layoutRequest.setExtraLayoutSpaceStart(scrapExtraStart)
        layoutRequest.setExtraLayoutSpaceEnd(scrapExtraEnd)

        if (scrapExtraStart > 0) {
            val anchor = layoutInfo.getChildClosestToStart()
            if (anchor != null) {
                architect.updateLayoutStateForPredictiveStart(
                    layoutRequest,
                    layoutManager.getPosition(anchor)
                )
                fill(layoutRequest, recycler, state)
            }
        }

        if (scrapExtraEnd > 0) {
            val anchor = layoutInfo.getChildClosestToEnd()
            if (anchor != null) {
                architect.updateLayoutStateForPredictiveEnd(
                    layoutRequest,
                    layoutManager.getPosition(anchor)
                )
                fill(layoutRequest, recycler, state)
            }
        }
    }

    override fun layoutExtraSpace(
        layoutRequest: LayoutRequest,
        preLayoutRequest: PreLayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        architect.updateForExtraLayoutStart(layoutRequest, state)
        fill(layoutRequest, recycler, state)

        architect.updateForExtraLayoutEnd(layoutRequest, state)
        fill(layoutRequest, recycler, state)
    }

    private fun addPivot(pivotPosition: Int, recycler: Recycler): View {
        val view = recycler.getViewForPosition(pivotPosition)
        layoutManager.addView(view)
        onChildLayoutListener.onChildCreated(view)
        return view
    }

    /**
     * Places the pivot in the correct layout position and returns its bounds via [bounds]
     */
    private fun updatePivotBounds(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest) {
        layoutManager.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.getParentKeyline()
        val headOffset = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)

        // Place the pivot in its keyline position
        if (layoutRequest.isVertical) {
            bounds.top = headOffset
            bounds.bottom = tailOffset
            applyHorizontalGravity(view, bounds, layoutRequest)
        } else {
            bounds.left = headOffset
            bounds.right = tailOffset
            applyVerticalGravity(view, bounds, layoutRequest)
        }
    }

    private fun layoutPivot(view: View, bounds: ViewBounds) {
        performLayout(view, bounds)
        if (DEBUG) {
            Log.i(TAG, "Laid pivot ${layoutInfo.getLayoutPositionOf(view)} at: $bounds")
        }
        bounds.setEmpty()
        onChildLayoutListener.onChildLaidOut(view)
    }

    private fun layoutFromPivotToStart(
        pivotView: View,
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        layoutRequest.prepend(pivotPosition) {
            setCheckpoint(layoutInfo.getDecoratedStart(pivotView))
            val startFillSpace = max(
                0, checkpoint - layoutInfo.getStartAfterPadding()
            )
            setFillSpace(startFillSpace + layoutRequest.extraLayoutSpaceStart)
        }
        fill(layoutRequest, recycler, state)
    }

    private fun layoutFromPivotToEnd(
        pivotView: View,
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        layoutRequest.append(pivotPosition) {
            setCheckpoint(layoutInfo.getDecoratedEnd(pivotView))
            val endFillSpace = max(
                0, layoutInfo.getEndAfterPadding() - checkpoint
            )
            setFillSpace(endFillSpace + layoutRequest.extraLayoutSpaceEnd)
        }
        fill(layoutRequest, recycler, state)
    }

    override fun layoutBlock(
        layoutRequest: LayoutRequest,
        recycler: RecyclerView.Recycler,
        state: State,
        layoutResult: LayoutResult
    ) {
        // Exit early if we're out of views to layout
        val view = layoutRequest.getNextView(recycler) ?: return
        addView(view, layoutRequest)
        onChildLayoutListener.onChildCreated(view)
        layoutManager.measureChildWithMargins(view, 0, 0)

        layoutResult.consumedSpace = if (layoutRequest.isLayingOutEnd()) {
            append(view, viewBounds, layoutRequest)
        } else {
            prepend(view, viewBounds, layoutRequest)
        }

        if (DEBUG) {
            Log.i(TAG, "Laid out view ${layoutInfo.getLayoutPositionOf(view)} at: $viewBounds")
        }

        if (shouldSkipSpaceOf(view)) {
            layoutResult.skipConsumption = true
        }

        performLayout(view, viewBounds)
        viewBounds.setEmpty()
        onChildLayoutListener.onChildLaidOut(view)
    }

    private fun append(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        if (layoutRequest.isVertical) {
            // We need to align this view to an edge or center it, depending on the gravity set
            applyHorizontalGravity(view, bounds, layoutRequest)
            bounds.top = layoutRequest.checkpoint
            bounds.bottom = bounds.top + decoratedSize
        } else {
            applyVerticalGravity(view, bounds, layoutRequest)
            bounds.left = layoutRequest.checkpoint
            bounds.right = bounds.left + decoratedSize
        }
        return decoratedSize
    }

    private fun prepend(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        if (layoutRequest.isVertical) {
            applyHorizontalGravity(view, bounds, layoutRequest)
            bounds.bottom = layoutRequest.checkpoint
            bounds.top = bounds.bottom - decoratedSize

        } else {
            applyVerticalGravity(view, bounds, layoutRequest)
            bounds.right = layoutRequest.checkpoint
            bounds.left = bounds.right - decoratedSize
        }
        return decoratedSize
    }

    private fun applyHorizontalGravity(
        view: View,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ) {
        val horizontalGravity = if (layoutRequest.reverseLayout) {
            Gravity.getAbsoluteGravity(
                layoutRequest.gravity.and(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK),
                View.LAYOUT_DIRECTION_RTL
            )
        } else {
            layoutRequest.gravity.and(Gravity.HORIZONTAL_GRAVITY_MASK)
        }
        when (horizontalGravity) {
            Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> {
                val width = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.left = layoutManager.width / 2 - width / 2
                bounds.right = bounds.left + width
            }
            Gravity.RIGHT -> {
                val width = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.right = layoutManager.width - layoutManager.paddingRight
                bounds.left = bounds.right - width
            }
            else -> { // Fallback to left gravity since this is the default expected behavior
                bounds.left = layoutManager.paddingLeft
                bounds.right = bounds.left + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }

    }

    private fun applyVerticalGravity(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest) {
        when (layoutRequest.gravity.and(Gravity.VERTICAL_GRAVITY_MASK)) {
            Gravity.CENTER, Gravity.CENTER_VERTICAL -> {
                val height = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.top = layoutManager.height / 2 - height / 2
                bounds.bottom = bounds.top + height
            }
            Gravity.BOTTOM -> {
                val height = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.bottom = layoutManager.height - layoutManager.paddingBottom
                bounds.top = bounds.bottom - height
            }
            else -> {  // Fallback to top gravity since this is the default expected behavior
                bounds.top = layoutManager.paddingTop
                bounds.bottom = bounds.top + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }
    }

}
