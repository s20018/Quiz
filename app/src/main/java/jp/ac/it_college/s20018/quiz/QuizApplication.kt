package jp.ac.it_college.s20018.quiz

import android.app.Application
import com.opencsv.bean.CsvToBeanBuilder
import com.opencsv.enums.CSVReaderNullFieldIndicator
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import jp.ac.it_college.s20018.quiz.csv.QuizRecord
import jp.ac.it_college.s20018.quiz.realm.model.Quiz

/**
 * 読み込む CSV ファイルのリスト
 */
private val quizFiles = listOf(
    "nakasone.csv",
    "s20003.csv",
    "s20007.csv",
    "s20008.csv",
    "s20010.csv",
    "s20011.csv",
    "s20012.csv",
    "s20014.csv",
    "s20015.csv",
    "s20016.csv",
    "s20019.csv",
    "s20020.csv",
    "s20022.csv",
    "s20024.csv",
)

@Suppress("unused")
class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true).build()
        Realm.setDefaultConfiguration(config)

        // ちょっと効率よくないですが、毎回 csv ファイルを読み込んでデータ件数をチェックする。
        val quizList = loadData()
        val csvQuizCount = quizList.size.toLong()

        // Realm に登録されているクイズデータの件数を取得
        val db = Realm.getDefaultInstance()
        val count = db.where<Quiz>().count()

        // クイズデータの件数が一致しない場合は、一度削除して登録をやり直す(初回と更新時に発動するはず)
        if (count != csvQuizCount) {
            registerQuizData(db, quizList)
        }
    }

    /**
     * CSVファイルからクイズデータのリストを取得して返します
     */
    private fun loadData(): List<QuizRecord> {
        val quizList = mutableListOf<QuizRecord>()
        for (file in quizFiles) {
            val reader = resources.assets.open(file).reader()

            // openCSV を利用してデータを取得
            val records = CsvToBeanBuilder<QuizRecord>(reader)
                .withType(QuizRecord::class.java)
                .withIgnoreLeadingWhiteSpace(true)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .withSkipLines(1)
                .build()
                .parse()
            quizList.addAll(records)
        }
        return quizList
    }

    /**
     * CSV のクイズデータを Realm に登録します
     * 既存のデータは消去されます。
     */
    private fun registerQuizData(db: Realm, csvList: List<QuizRecord>) {
        db.executeTransaction { transaction ->
            transaction.where<Quiz>().findAll().deleteAllFromRealm()

            for (i in csvList.indices) {
                val quiz = transaction.createObject<Quiz>(i + 1)
                quiz.apply {
                    question = csvList[i].question
                    imageFilename = csvList[i].imageFilename?.substringBeforeLast(".")
                    imageCopyright = csvList[i].imageCopyright
                    choices = RealmList<String>(
                        csvList[i].choice1,
                        csvList[i].choice2,
                        csvList[i].choice3,
                        csvList[i].choice4,
                    )
                }
            }
        }
    }
}