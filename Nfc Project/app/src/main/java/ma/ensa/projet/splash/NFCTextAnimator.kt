package ma.ensa.projet.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.text.SpannableString
import android.widget.TextView

class NFCTextAnimator(private val textView: TextView) {
    private val letterN = ObjectAnimator()
    private val letterF = ObjectAnimator()
    private val letterC = ObjectAnimator()

    init {
        setupAnimations()
    }

    private fun setupAnimations() {
        // Configuration initiale du TextView
        textView.text = ""

        // Animation pour la lettre N
        val nSpan = SpannableString("N")
        letterN.apply {
            duration = 500
            addUpdateListener {
                textView.text = nSpan
                textView.translationY = it.animatedValue as Float
            }
            setFloatValues(100f, 0f)
        }

        // Animation pour la lettre F
        val fSpan = SpannableString("NF")
        letterF.apply {
            duration = 500
            startDelay = 500
            addUpdateListener {
                textView.text = fSpan
                textView.translationY = it.animatedValue as Float
            }
            setFloatValues(-100f, 0f)
        }

        // Animation pour la lettre C
        val cSpan = SpannableString("NFC")
        letterC.apply {
            duration = 500
            startDelay = 1000
            addUpdateListener {
                textView.text = cSpan
                textView.translationX = it.animatedValue as Float
            }
            setFloatValues(100f, 0f)
        }
    }

    fun start() {
        AnimatorSet().apply {
            playSequentially(letterN, letterF, letterC)
            start()
        }
    }
}