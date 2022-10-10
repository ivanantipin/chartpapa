package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd

interface IButton{
    val name : String
    val data : Cmd
    fun children() : List<IButton>
    fun register(menuRegistry: MenuRegistry)
}