import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 */
public class ProxyThread extends Thread {
    private Socket serverClient;
    ProxyThread(Socket serverClient){
        this.serverClient = serverClient;
        this.start();
    }

    @Override
    public void run() {
        try {
            DataOutputStream out = new DataOutputStream(serverClient.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(serverClient.getInputStream()));

            String lineIn, lineOut;
            int count = 0;
            String urlToCall = "";

            while ((lineIn = in.readLine()) != null) {
                try {
                    StringTokenizer token = new StringTokenizer(lineIn);
                    token.nextToken();
                } catch (Exception e) {
                    break;
                }
                //Parse the first line
                if (count  == 0) {
                    String[] tokens = lineIn.split(" ");
                    urlToCall = tokens[1];

                    System.out.println("Request for : " + urlToCall);
                }
                count++;
            }
        } catch (Exception e){

        };
    }
}
