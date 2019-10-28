package ru.raid.miptandroid

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class NoteLandscapeViewDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildLayoutPosition(view) % 2 == 0) {
            outRect.right = spacing / 2
        } else {
            outRect.left = spacing / 2
        }
    }
}