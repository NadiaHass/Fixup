package com.nadiahassouni.fixup.models

import java.io.Serializable

class User (
    var id : String = "",
    var name : String = "",
    var surname : String = "",
    var sexe : String = "",
    var imageUrl : String = "",
    var email : String = "",
    var tel : String = "",
    var wilaya : String = "",
    var description : String = "",
    var role : String = ""
        ) : Serializable