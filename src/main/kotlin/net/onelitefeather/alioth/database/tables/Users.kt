package net.onelitefeather.alioth.database.tables

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {

    val uniqueId = uuid("id")
    val name = varchar("name", length = 16)

    override val primaryKey = PrimaryKey(arrayOf(uniqueId, name), "id_name")
}