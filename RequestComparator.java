import java.util.Comparator;

public class RequestComparator implements Comparator<RequestClient> {
    public int compare(RequestClient a, RequestClient b){
        if(a.timeStamp < b.timeStamp){
            return 1;
        }

        else if(a.timeStamp == b.timeStamp && Integer.valueOf(a.clientId) <= Integer.valueOf(b.clientId)){
            return 1;
        }

        else {
            return -1;
        }
    }
}
