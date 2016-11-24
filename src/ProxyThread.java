import com.sun.org.apache.xpath.internal.SourceTree;

import javax.net.ssl.HttpsURLConnection;
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStream proxyClient = serverClient.getOutputStream();
            InputStream in = serverClient.getInputStream();

            int input;
            int count = 0;
            String urlToCall = "";

            while ((input = in.read()) != -1) {
                if(input == '\n') break;
                else out.write(input);
            }

            String line = out.toString("UTF-8");
            line = line.replaceAll("\\r", "");
            System.out.println(line);
            //Parse the first line
            String[] tokens = line.split(" ");
            urlToCall = tokens[1];
            System.out.println("urlToCall: " + urlToCall);
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


            /////////////////////////////////

            BufferedReader reader = null;
            try {
                //begin send request to server, get response from server
                if(urlToCall.equals("")){
                    throw new SocketException();
                }
                URL url = new URL(urlToCall);
                HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
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
                //} catch (SSLHandshakeException e){
                } catch (IOException err) {
                    System.out.println(urlToCall);
                    System.err.println("IO EXCEPTION!!: " + err);
                }


                //Send headers
                proxyClient.write(("HTTP/1.1 200 OK\r\nContent-Type: " + httpsConnection.getContentType() + "\r\n\r\n").getBytes());
                        //getHeaderField("Content-Type") + "\r\n\r\n").getBytes());

                //Begin send response to client
                byte[] by = new byte[BUFFER_SIZE];
                //assert inStream != null;
                try {
                    int index = inStream.read(by, 0, BUFFER_SIZE);
                    while (index != -1) {
                        //System.out.println(new String(by));
                        proxyClient.write(by, 0, index);
                        index = inStream.read(by, 0, BUFFER_SIZE);
                    }
                    System.out.println("Sent");
                } catch (NullPointerException e) {
                    System.out.println("Ya BITCH");
                }
                proxyClient.flush();
            //} catch (SocketException e){

            //} catch (SSLHandshakeException e){

            } catch (Exception e) {
                System.err.println("Encountered exception: " + e + " --- " + urlToCall + " url given: " + out);
                e.printStackTrace();
            }
            //close out all resources
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
