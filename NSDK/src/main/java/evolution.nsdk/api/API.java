package evolution.nsdk.api;

public abstract class API {

    protected final String baseURL = "http://localhost:8080";
    protected final int idKey;

    public API(int idKey) {
        this.idKey = idKey;
    }
}
