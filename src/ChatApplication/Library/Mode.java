package ChatApplication.Library;

// mode for a message
public enum Mode {
    CHANGE_NAME,
    CHANGE_PASSWORD,
    DISCONNECT,
    ERROR,
    INFORMATION_REQUEST,
    LOGIN,
    LOGOUT,
    MESSAGE,
    REGISTRATION,
    ROOM_CREATE,    //für Server
    ROOM_JOIN,      //für Server
    USER_TRANSMIT,  //für Client
    ROOM_TRANSMIT   //für Client
}

