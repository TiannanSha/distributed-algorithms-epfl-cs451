package cs451;

import java.io.*;

public class ConfigParser {

    private String path;

    // config for perfectLink test
    private int numMsgsToSend;
    private int hostIdToSendTo;

    public boolean populate(String value) {
        File file = new File(value);
        path = file.getPath();
        return true;
    }

    public String getPath() {
        return path;
    }

    public void readPerfectLinkConf() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = reader.readLine();
            String[] splits = line.split(" ");
            numMsgsToSend = Integer.parseInt(splits[0]);
            hostIdToSendTo = Integer.parseInt(splits[1]);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getNumMsgsToSend() {
        return numMsgsToSend;
    }

    public int getHostIdToSendTo() {
        return hostIdToSendTo;
    }
}
