package jp.ac.it_college.s20018.quiz.csv

import com.opencsv.bean.CsvBindByPosition

data class QuizRecord(
    @CsvBindByPosition(position = 0)
    val question: String = "",

    @CsvBindByPosition(position = 1)
    val imageFilename: String? = null,

    @CsvBindByPosition(position = 6)
    val imageCopyright: String? = null,

    @CsvBindByPosition(position = 2)
    val choice1: String = "",

    @CsvBindByPosition(position = 3)
    val choice2: String = "",

    @CsvBindByPosition(position = 4)
    val choice3: String = "",

    @CsvBindByPosition(position = 5)
    val choice4: String = ""
)