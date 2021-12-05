package jp.ac.it_college.s20018.quiz.fragment

import android.animation.ObjectAnimator
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import jp.ac.it_college.s20018.quiz.databinding.FragmentQuizBinding
import jp.ac.it_college.s20018.quiz.realm.model.Quiz
import jp.ac.it_college.s20018.quiz.realm.model.randomChooseQuiz

/** あああ
 */

class QuizFragment : Fragment() {
    companion object {
        const val MAX_COUNT = 10
        const val TIME_LIMIT = 10000L
        const val TIMER_INTERVAL = 100L
        const val CHOICE_DELAY_TIME = 2000L
        const val TIME_UP_DELAY_TIME = 1500L
    }

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private var current = -1
    private var timeLeftCountdown = TimeLeftCountdown()
    private var startTime = 0L
    private var totalElapsedTime = 0L
    private var correctCount = 0
    private val currentElapsedTime get() = SystemClock.elapsedRealtime() - startTime

    private lateinit var quizList: List<Quiz>

    /**
     * 選択肢ボタンで使用する共通のイベントリスナー
     */
    private val onChoiceClick = View.OnClickListener { v ->
        if (v !is Button) return@OnClickListener // Button じゃなければ終了。スマートキャスト

        isBulkEnableButton(false)
        timeLeftCountdown.cancel()
        totalElapsedTime += currentElapsedTime

        if (v.text == quizList[current].choices[0]) {
            // 正解パターン
            binding.goodIcon.visibility = View.VISIBLE
            correctCount++
            delayNext(CHOICE_DELAY_TIME)
        } else {
            // 不正解パターン
            binding.badIcon.visibility = View.VISIBLE
            delayNext(CHOICE_DELAY_TIME)
        }
    }

    /**
     * 制限時間をカウントダウンするタイマー
     */
    inner class TimeLeftCountdown : CountDownTimer(TIME_LIMIT, TIMER_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            animateToProgress(millisUntilFinished.toInt())
        }

        override fun onFinish() {
            totalElapsedTime += TIME_LIMIT
            isBulkEnableButton(false)
            animateToProgress(0)
            binding.timeupIcon.visibility = View.VISIBLE
            delayNext(TIME_UP_DELAY_TIME)
        }

        /**
         * API Level 24 であれば、ProgressBar 自体にアニメーションのパラメータがありますが
         * 今回は 23 なので、ObjectAnimator を使って実装
         */
        private fun animateToProgress(progress: Int) {
            val anim = ObjectAnimator.ofInt(binding.timeLeftBar, "progress", progress)
            anim.duration = TIMER_INTERVAL
            anim.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quizList = randomChooseQuiz(MAX_COUNT)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // クイズの途中に Back ボタンでタイトルへ戻れないよう
            // 何も処理をしない対策
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 全てのボタンで同一のイベントリスナを使用
        binding.choiceA.setOnClickListener(onChoiceClick)
        binding.choiceB.setOnClickListener(onChoiceClick)
        binding.choiceC.setOnClickListener(onChoiceClick)
        binding.choiceD.setOnClickListener(onChoiceClick)

        next()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timeLeftCountdown.cancel()
        _binding = null
    }

    /**
     * 次の問題へ進む処理。
     * 問題がなければリザルトへ進む。
     */
    private fun next() {
        // 次の問題がまだある場合
        if (++current < MAX_COUNT) {
            timeLeftCountdown.cancel()
            binding.timeLeftBar.progress = 10000
            binding.badIcon.visibility = View.GONE
            binding.goodIcon.visibility = View.GONE
            binding.timeupIcon.visibility = View.GONE
            setQuiz(current)
            isBulkEnableButton(true)
            timeLeftCountdown.start()
            startTime = SystemClock.elapsedRealtime()
            return
        }

        //次の問題がもう無い場合
        val action = QuizFragmentDirections.actionToResult(correctCount, totalElapsedTime)
        findNavController().navigate(action)
    }

    /**
     * next() を遅延実行するためのメソッド
     */
    private fun delayNext(delay: Long) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(delay) {
            next()
        }
    }

    /**
     * 選択肢ボタンの isEnabled プロパティを一括で変更するメソッド
     */
    private fun isBulkEnableButton(flag: Boolean) {
        binding.choiceA.isEnabled = flag
        binding.choiceB.isEnabled = flag
        binding.choiceC.isEnabled = flag
        binding.choiceD.isEnabled = flag
    }

    /**
     * 指定の番号で、各ビューに問題データをセットするメソッド
     */
    private fun setQuiz(position: Int) {
        binding.quizText.text = quizList[position].question
        if (quizList[position].imageFilename.isNullOrBlank()) {
            binding.quizImage.visibility = View.GONE
            binding.copyrightText.visibility = View.GONE
        } else {
            val resId = resources.getIdentifier(
                quizList[position].imageFilename, "drawable",
                context?.packageName
            )
            binding.quizImage.apply {
                setImageResource(resId)
                visibility = View.VISIBLE
            }
            binding.copyrightText.apply {
                text = quizList[position].imageCopyright
                visibility = View.VISIBLE
            }
        }

        val randomChoice = quizList[position].choices.shuffled()
        binding.choiceA.text = randomChoice[0]
        binding.choiceB.text = randomChoice[1]
        binding.choiceC.text = randomChoice[2]
        binding.choiceD.text = randomChoice[3]
    }
}
