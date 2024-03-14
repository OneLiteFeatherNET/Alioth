package net.onelitefeather.alioth.database.tables

import org.bukkit.entity.Villager
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Traders : Table("traders") {

    val uniqueId = uuid("id")
    val type = enumerationByName("v_type", 255, Villager.Type::class).default(Villager.Type.PLAINS)
    val prof = enumerationByName("v_prof", 255, Villager.Profession::class).default(Villager.Profession.FARMER)
    val display = text("display").default("&6Shop")
    val owner = reference("owner", Users.uniqueId, ReferenceOption.CASCADE)
    val location = text("location").default("{}")
    val level = integer("level").default(1)

    override val primaryKey = PrimaryKey(arrayOf(uniqueId, owner), "id_owner")

}