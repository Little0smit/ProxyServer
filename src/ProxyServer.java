import java.io.IOException;
import java.net.ServerSocket;

/**
 */
public class ProxyServer {

    public static void main(String[] args){
        boolean listening = true;
        try {
            int port = Integer.parseInt(args[0]);

            // Start the server
            ServerSocket proxyServer = new ServerSocket(port);
            System.out.println("Started on port: " + port);
            do {
                new ProxyThread(proxyServer.accept()).start();
            } while(listening);
            proxyServer.close();
        } catch (IOException err){

        }

    }

}
