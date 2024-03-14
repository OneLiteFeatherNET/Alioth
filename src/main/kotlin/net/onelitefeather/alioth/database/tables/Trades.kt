package net.onelitefeather.alioth.database.tables

import org.jetbrains.exposed.sql.Table

object Trades : Table("trades") {

    val id = long("id").autoIncrement("trade_id_seq")
    val item = text("item").default("")
    val buyable = bool("buyable")
    val trader = reference("trader_id", Traders.uniqueId)

    override val primaryKey = PrimaryKey(arrayOf(id), "id")

}