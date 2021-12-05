package jp.ac.it_college.s20018.quiz.realm.model

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

open class Quiz : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var question: String = ""
    var imageFilename: String? = null
    var imageCopyright: String? = null
    var choices: RealmList<String> = RealmList()
}

/**
 * 全クイズデータを取得してシャッフルし
 * 件数をチェックしつつ指定の件数以下でリストを返す
 */
fun randomChooseQuiz(count: Int): List<Quiz> {
    val list = Realm.getDefaultInstance().where<Quiz>().findAll().shuffled()

    return if (list.size < count) list
    else list.subList(0, count)
}