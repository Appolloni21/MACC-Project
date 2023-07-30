package com.example.macc.utility


import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

private const val TAG = "Dialog Fragment"

class UIDialogFragment(title: String) : DialogFragment() {

    //Title of the alert
    private var titleStr: String = title

    // Use this instance of the interface to deliver action events
    private lateinit var listener: NoticeDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Are you sure to delete $titleStr?")
                .setPositiveButton("Yes",
                    DialogInterface.OnClickListener { dialog, id ->
                        // START THE GAME!
                        Log.d(TAG,"Dialog Yes, $dialog, $id")
                        listener.onDialogPositiveClick(this)

                    })
                .setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                        Log.d(TAG,"Dialog cancel no $dialog, $id")
                        listener.onDialogNegativeClick(this)

                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}