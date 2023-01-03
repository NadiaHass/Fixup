package com.nadiahassouni.fixup.models

import java.io.Serializable

class Reclamation (
    var id : String = "",
    var clientId : String = "",
    var agentId : String = "",
    var title : String = "" ,
    var problemType : String = "",
    var description : String = "",
    var date : String = "",
    var state : String = ""
        ) : Serializable