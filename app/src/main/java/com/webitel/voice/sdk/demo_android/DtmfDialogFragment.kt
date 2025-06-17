package com.webitel.voice.sdk.demo_android

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment


class DtmfDialogFragment : DialogFragment() {

    /**
     * Interface for passing DTMF digits back to the host activity.
     */
    interface OnDtmfDataEntered {
        fun onDTMFEntered(value: String)
    }

    private var listener: OnDtmfDataEntered? = null
    private val inputHistory = StringBuilder()

    private lateinit var inputHistoryTextView: TextView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            context is OnDtmfDataEntered -> context
            parentFragment is OnDtmfDataEntered -> parentFragment as OnDtmfDataEntered
            else -> throw RuntimeException("$context must implement OnDtmfDataEntered")
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dtmfHistory = arguments?.getString("dtmfHistory") ?: ""
        inputHistory.append(dtmfHistory)

        val rootView = LayoutInflater.from(context).inflate(R.layout.dialog_dtmf, null)
        dialog.setContentView(rootView)

        inputHistoryTextView = rootView.findViewById(R.id.tvHistory)
        inputHistoryTextView.text = inputHistory.toString()
        val gridLayout = rootView.findViewById<GridLayout>(R.id.dtmfGrid)

        val dtmfButtons = listOf(
            "1", "2", "3",
            "4", "5", "6",
            "7", "8", "9",
            "*", "0", "#"
        )

        dtmfButtons.forEach { symbol ->
            val button = Button(context).apply {
                text = symbol
                textSize = 20f
                setPadding(0, 16, 0, 16)
                setOnClickListener { onDtmfPressed(symbol) }
            }

            val params = GridLayout.LayoutParams().apply {
                width = dpToPx(60)
                height = dpToPx(60)
                setMargins(8, 8, 8, 8)
            }
            button.layoutParams = params
            gridLayout.addView(button)
        }

        return dialog
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }


    /**
     * Called when a DTMF button is pressed. Appends to history and notifies the listener.
     */
    private fun onDtmfPressed(value: String) {
        inputHistory.append(value)
        inputHistoryTextView.text = inputHistory.toString()
        listener?.onDTMFEntered(value)
    }
}