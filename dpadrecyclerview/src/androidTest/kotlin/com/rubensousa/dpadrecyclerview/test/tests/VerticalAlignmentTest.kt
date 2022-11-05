package com.rubensousa.dpadrecyclerview.test.tests

import android.view.Gravity
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.KeyPresser
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class VerticalAlignmentTest : GridTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = 0,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
    }

    @Test
    fun testMiddleItemsAreAlignedToContainerOffsets() {
        launchFragment()
        KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
            KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
            position++
        }

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 100
            )
        )
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() + 100)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 0
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = -100
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() - 100)
    }

    @Test
    fun testMiddleItemsAreAlignedToItemOffsets() {
        launchFragment()
        KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
            KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
            position++
        }

        updateChildAlignment(ChildAlignment(offset = 100))
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() + 100)

        updateChildAlignment(ChildAlignment(offset = 0))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateChildAlignment(ChildAlignment(offset = -100))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() - 100)
    }

    @Test
    fun testFirstItemAlignmentForEdgeAlignments() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(viewBounds.height() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(viewBounds.height() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
    }

    @Test
    fun testLastItemAlignmentForEdgeAlignments() {
        launchFragment()
        val lastPosition = selectLastPosition()
        val recyclerViewBounds = getRecyclerViewBounds()

        var viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)
    }

    @Test
    fun testItemsAreAlignedToContainerOffset() {
        val offset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = offset,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0f
            )
        )
        KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.top).isEqualTo(recyclerViewBounds.centerY() + offset)
            KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testItemsAreAlignedToBothContainerAndItemAlignmentOffsets() {
        val containerOffset = 100
        val itemOffset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = containerOffset,
                offsetRatio = 0f
            ),
            childAlignment = ChildAlignment(
                offset = itemOffset,
                offsetRatio = 0f
            )
        )
        KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.top)
                .isEqualTo(recyclerViewBounds.top + containerOffset + itemOffset)
            KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testItemsAreAlignedToBothContainerAndItemAlignmentOffsetPercentages() {
        val containerOffset = 100
        val itemOffset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = containerOffset,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = itemOffset,
                offsetRatio = 0.5f
            )
        )
        KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.centerY())
                .isEqualTo(recyclerViewBounds.centerY() + containerOffset + itemOffset)
            KeyPresser.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testGravityAffectsBoundsOfItems() {
        val parentAlignment = ParentAlignment(
            edge = Edge.NONE,
            offset = 0,
            offsetRatio = 0.5f
        )
        val layoutConfig = TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            gravity = Gravity.CENTER,
            parentAlignment = parentAlignment,
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
        val adapterConfig = TestAdapterConfiguration(
            itemLayoutId = R.layout.test_item_horizontal
        )
        launchFragment(layoutConfig, adapterConfig)

        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        onRecyclerView("Changing gravity to END") { recyclerView ->
            recyclerView.setGravity(Gravity.END)
        }

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)

        onRecyclerView("Changing gravity to START") { recyclerView ->
            recyclerView.setGravity(Gravity.START)
        }

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.left).isEqualTo(recyclerViewBounds.left)
    }

}
