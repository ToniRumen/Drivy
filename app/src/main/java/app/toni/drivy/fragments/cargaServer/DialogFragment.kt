package app.toni.drivy.fragments.cargaServer

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import app.toni.drivy.R

class ServerWaitDialogFragment : DialogFragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var textStatus: TextView
    private var secondsPassed = 0
    private val maxSeconds = 150 // 2min30seg

    private val timer = object : CountDownTimer((maxSeconds * 1000).toLong(), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            secondsPassed++
            progressBar.progress = secondsPassed
        }
        override fun onFinish() {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_wait_server, null)
        progressBar = view.findViewById(R.id.progressBar)
        textStatus = view.findViewById(R.id.textStatus)
        progressBar.max = maxSeconds
        progressBar.progress = 0
        secondsPassed = 0

        val builder = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        timer.start()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        timer.cancel()
    }
}
