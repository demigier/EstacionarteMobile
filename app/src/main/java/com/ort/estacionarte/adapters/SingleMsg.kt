package com.ort.estacionarte.adapters

class SingleMsg(private var msg: String) {
    private var new: Boolean = true

    fun readMsg(): String {
        if (new) {
            new = false
        } else {
            msg = ""
        }

        return msg
    }

    fun isNew(): Boolean {
        return new
    }

}