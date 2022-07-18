public class RequestClient {
    public String clientId;
    public long timeStamp;

    public RequestClient(String clientId, long timeStamp) {
        this.clientId = clientId;
        this.timeStamp = timeStamp;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

}
