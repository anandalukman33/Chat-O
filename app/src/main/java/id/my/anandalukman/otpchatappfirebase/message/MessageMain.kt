package id.my.anandalukman.otpchatappfirebase.message

class MessageMain {

    var messageId : String? = null
    var message : String? = null
    var senderId : String? = null
    var imageUrl : String? = null
    var timeStamp : Long = 0

    constructor(){}
    constructor(
        message : String?,
        senderId : String?,
        timeStamp : Long = 0
    ) {
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
    }

}