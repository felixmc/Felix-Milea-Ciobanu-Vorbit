package com.felixmilea.vorbit.main

import com.felixmilea.vorbit.utils.Log

object Vorbit extends App {

  Log.init()
  Log.Info("Vorbit started.")
  Log.Debug("debugging something")
  Log.Error("Shoot..something bad happened")
  Log.Fatal("ERR MER GERR...CRASHINGGGG")

}