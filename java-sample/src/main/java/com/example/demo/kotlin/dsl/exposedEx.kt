package com.example.demo.kotlin.dsl

// Table 내부에서 컬럼에 대한 기능을 확장하여 테이블에서만 사용할 수 있도록 한다.
open class Table {
    fun integer(name: String) = Column<Int>()
    fun varchar(name: String, length: Int) = Column<String>()

    fun <T> Column<T>.primaryKey(): Column<T> = TODO()
    // 자동 증가는 int만 되도록 제한을 건다
    fun Column<Int>.autoIncrement(): Column<Int> = TODO()
}

class Column<T>

// Table에서 정의한 타입들을 활용하여 컬럼을 지정할 수 있다.
object Item: Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 50)
}
fun main() {
    // 컬럼에 필요한 함수들은 Table 내부에서 확장하여 사용하기 때문에 외부에선 호출을 불가능하게 캡슐화할 수 있다.
    // Column<Int>().primaryKey()
}
