import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 */
public class ProxyThread extends Thread {
    private Socket serverClient;
    private static final int BUFFER_SIZE = 32768;
    ProxyThread(Socket serverClient){
        super("ProxyThread");
        this.serverClient = serverClient;
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
            /////////////////////////////////

            BufferedReader reader = null;
            try {
                //begin send request to server, get response from server
                URL url = new URL(urlToCall);
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setDoInput(true);
                httpConnection.setDoOutput(false);

                //Get the response]
                InputStream inStream = null;
                try {
                    switch (httpConnection.getResponseCode()) {
                        case 200:
                            inStream = httpConnection.getInputStream();
                            reader = new BufferedReader(new InputStreamReader(inStream));
                            break;
                        case 404:
                            System.out.println("404 Page not found");

                        default:
                            break;
                    }
                } catch (IOException err) {
                    System.err.println("IO EXCEPTION!!: " + err);
                }

                System.out.println();

                //Begin send response to client
                byte[] by = new byte[BUFFER_SIZE];
                assert inStream != null;
                try {
                    int index = inStream.read(by, 0, BUFFER_SIZE);
                    while (index != -1) {
                        out.write(by, 0, index);
                        index = inStream.read(by, 0, BUFFER_SIZE);
                    }
                } catch (NullPointerException e) {

                }
                out.flush();

            } catch (Exception e) {
                System.err.println("Encountered exception: " + e);
                e.printStackTrace();
                out.writeBytes("");
            }
            //close out all resources
            if (reader != null) {
                reader.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (serverClient != null) {
                serverClient.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        };
    }
}
