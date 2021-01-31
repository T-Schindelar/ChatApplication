package ChatApplication.Library;

// mode for a message
public enum Mode {
    CHANGE_NAME,
    CHANGE_PASSWORD,
    DELETE_ACCOUNT,
    DISCONNECT,
    ERROR,
    INFORMATION_REQUEST,
    LOGIN,
    LOGOUT,
    MESSAGE,
    MESSAGE_PRIVATE,
    REGISTRATION,
    ROOM_CREATE,            // for server
    ROOM_CREATE_PRIVATE,    // for server
    ROOM_JOIN,              // for server
    USER_TRANSMIT,          // for client
    ROOM_TRANSMIT,          // for client
    UPDATE_ROOM             // for client
}

