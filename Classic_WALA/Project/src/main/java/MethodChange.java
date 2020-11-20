import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MethodChange {
    private HashMap<String, String> changeInfo;


    MethodChange(String path) throws IOException {
        changeInfo = new HashMap<String, String>();
        File file = new File(path);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] change = line.split(" ");
            this.changeInfo.put(change[1], change[0]);
        }
    }

    public HashMap<String, String> getChangeInfo() {
        return changeInfo;
    }
}
