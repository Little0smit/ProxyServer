import com.sun.org.apache.xpath.internal.SourceTree;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.*;
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
                    String[] urlToCallTokens = urlToCall.split(":",2);
                    if(!urlToCallTokens[0].equals("https")){
                        if(urlToCallTokens[0].contains("http")){
                            urlToCallTokens[0] = "https:";
                        } else {
                            urlToCallTokens[1] = urlToCallTokens[0] + ":" + urlToCallTokens[1];
                            urlToCallTokens[0] = "https://";
                        }
                        urlToCall = urlToCallTokens[0] + urlToCallTokens[1];
                    }
                    System.out.println("Request for : " + urlToCall);
                }
                count++;
            }
            /////////////////////////////////

            BufferedReader reader = null;
            try {
                //begin send request to server, get response from server
                if(urlToCall.equals("")){
                    throw new SocketException();
                }
                URL url = new URL(urlToCall);
                HttpURLConnection httpsConnection = (HttpURLConnection) url.openConnection();
                httpsConnection.setDoInput(true);
                httpsConnection.setDoOutput(false);

                //Get the response]
                InputStream inStream = null;
                try {
                    System.out.println("Response code: " + httpsConnection.getResponseCode() + " from: " + urlToCall);
                    System.out.println("Content Type: " + httpsConnection.getContentType());
                    System.out.println("Content: " + httpsConnection.getContent().toString());
                    switch (httpsConnection.getResponseCode()) {
                        case 200:
                            inStream = httpsConnection.getInputStream();
                            break;
                        case 403:
                            inStream = httpsConnection.getInputStream();
                            break;
                        case 400:
                            inStream = httpsConnection.getInputStream();
                            break;
                        case 404:
                            System.out.println("404 Page not found");
                        default:
                            System.out.println(httpsConnection.getResponseCode());
                            break;
                    }
                } catch (IOException err) {
                    System.err.println("IO EXCEPTION!!: " + err);
                }
                //Send headers
                out.write(("HTTP/1.1 200 OK\r\nContent-Type: " + httpsConnection.getContentType() + "\r\n\r\n").getBytes());
                //getHeaderField("Content-Type") + "\r\n\r\n").getBytes());

                //Begin send response to client
                byte[] by = new byte[BUFFER_SIZE];
                try {
                    int index = inStream.read(by, 0, BUFFER_SIZE);
                    while (index != -1) {
                        out.write(by, 0, index);
                        index = inStream.read(by, 0, BUFFER_SIZE);
                    }
                } catch (NullPointerException e) {

                }
                out.flush();
            } catch (SSLHandshakeException e){
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
