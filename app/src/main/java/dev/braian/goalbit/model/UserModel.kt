package dev.braian.goalbit.model

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UserModel(){
    var id:String = ""
    var name:String = ""
    var photoUrl:String  = ""

    fun save(){
        var reference: DatabaseReference = FirebaseDatabase.getInstance().reference

        reference.child("users").child(id).setValue(this)
    }
}