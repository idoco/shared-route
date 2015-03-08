package sharedroute;

/**
* Created by cohid01 on 08/03/2015.
*/
class ConnectionWrapper {
    String sessionId;
    String wsBinaryHandlerID;

    ConnectionWrapper(String userId, String wsBinaryHandlerID) {
        this.sessionId = userId;
        this.wsBinaryHandlerID = wsBinaryHandlerID;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionWrapper that = (ConnectionWrapper) o;

        return !(sessionId != null ? !sessionId.equals(that.sessionId) : that.sessionId != null);

    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}
