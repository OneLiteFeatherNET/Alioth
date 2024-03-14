package net.onelitefeather.alioth

import net.onelitefeather.alioth.listener.AliothVillagerListener
import net.onelitefeather.alioth.villager.VillagerManager
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin

class Alioth : JavaPlugin() {

    private lateinit var villagerManager: VillagerManager

    override fun onLoad() {
        //saveDefaultConfig()
    }

    override fun onEnable() {
        villagerManager = VillagerManager(this)
        server.pluginManager.registerEvents(AliothVillagerListener(this, villagerManager), this)
    }

    override fun onDisable() {

    }

}