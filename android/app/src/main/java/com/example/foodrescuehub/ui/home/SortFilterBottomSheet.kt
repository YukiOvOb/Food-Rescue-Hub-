package com.example.foodrescuehub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.example.foodrescuehub.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.RangeSlider

/**
 * Bottom sheet for sort and filter options
 * Allows users to:
 * - Sort by name, price, or popularity
 * - Filter by price range
 */
class SortFilterBottomSheet(
    private val currentSortOption: SortOption,
    private val onApply: (SortOption, Float, Float) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var rgSortOptions: RadioGroup
    private lateinit var rbNameAsc: RadioButton
    private lateinit var rbNameDesc: RadioButton
    private lateinit var rbPriceLowHigh: RadioButton
    private lateinit var rbPriceHighLow: RadioButton
    private lateinit var rbPopularity: RadioButton
    private lateinit var rbDistance: RadioButton
    private lateinit var priceRangeSlider: RangeSlider
    private lateinit var btnApply: Button
    private lateinit var btnReset: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_sort_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupCurrentSelection()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        rgSortOptions = view.findViewById(R.id.rgSortOptions)
        rbNameAsc = view.findViewById(R.id.rbNameAsc)
        rbNameDesc = view.findViewById(R.id.rbNameDesc)
        rbPriceLowHigh = view.findViewById(R.id.rbPriceLowHigh)
        rbPriceHighLow = view.findViewById(R.id.rbPriceHighLow)
        rbPopularity = view.findViewById(R.id.rbPopularity)
        rbDistance = view.findViewById(R.id.rbDistance)
        priceRangeSlider = view.findViewById(R.id.priceRangeSlider)
        btnApply = view.findViewById(R.id.btnApply)
        btnReset = view.findViewById(R.id.btnReset)
    }

    private fun setupCurrentSelection() {
        // Set current sort option
        when (currentSortOption) {
            SortOption.NAME_ASC -> rbNameAsc.isChecked = true
            SortOption.NAME_DESC -> rbNameDesc.isChecked = true
            SortOption.PRICE_LOW_HIGH -> rbPriceLowHigh.isChecked = true
            SortOption.PRICE_HIGH_LOW -> rbPriceHighLow.isChecked = true
            SortOption.POPULARITY -> rbPopularity.isChecked = true
            SortOption.DISTANCE -> rbDistance.isChecked = true
        }
    }

    private fun setupClickListeners() {
        btnApply.setOnClickListener {
            val selectedSort = when (rgSortOptions.checkedRadioButtonId) {
                R.id.rbNameAsc -> SortOption.NAME_ASC
                R.id.rbNameDesc -> SortOption.NAME_DESC
                R.id.rbPriceLowHigh -> SortOption.PRICE_LOW_HIGH
                R.id.rbPriceHighLow -> SortOption.PRICE_HIGH_LOW
                R.id.rbPopularity -> SortOption.POPULARITY
                R.id.rbDistance -> SortOption.DISTANCE
                else -> SortOption.NAME_ASC
            }

            val priceRange = priceRangeSlider.values
            val minPrice = priceRange[0]
            val maxPrice = priceRange[1]

            onApply(selectedSort, minPrice, maxPrice)
            dismiss()
        }

        btnReset.setOnClickListener {
            rbNameAsc.isChecked = true
            priceRangeSlider.setValues(0f, 50f)
        }
    }

    companion object {
        const val TAG = "SortFilterBottomSheet"
    }
}
