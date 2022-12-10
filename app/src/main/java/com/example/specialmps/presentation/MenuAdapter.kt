package com.example.specialmps.presentation

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import nl.psdcompany.duonavigationdrawer.views.DuoOptionView

class MenuAdapter(options: ArrayList<String>) : BaseAdapter() {
    private var mOptions = ArrayList<String>()
    private val mOptionViews = ArrayList<DuoOptionView>()

    override fun getCount(): Int {
        return mOptions.size
    }

    fun setViewSelected(position: Int, selected: Boolean) {
        //
        for (i in mOptionViews.indices) {
            if (i == position) {
                mOptionViews[i].isSelected = selected
            } else {
                mOptionViews[i].isSelected = !selected
            }
        }
    }

    override fun getItem(p0: Int): Any {
        return mOptions[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val option = mOptions[p0]//p0==position

        val optionView: DuoOptionView
        optionView = if (p1 == null) { //p1==convertView
            DuoOptionView(p2!!.context) //p2==parent
        } else {
            p1 as DuoOptionView
        }

        optionView.bind(option, null, null)

        mOptionViews.add(optionView)
        return optionView
    }

    init {
        mOptions = options
    }
}