import java.io.IOException;
import java.net.ServerSocket;

/**
 */
public class ProxyServer {

    public static void main(String[] args){
        try {
            if (args.length != 3){
                System.out.println("Insufficient arguments");
            }
            String hostURL = args[0];
            int localPort = Integer.parseInt(args[1]);
            int remotePort = Integer.parseInt(args[2]);
            System.out.println("Starting proxy server for " + hostURL + ":" + remotePort + " on port: " +  localPort);

            // Start the server
            ServerSocket proxyServer = new ServerSocket(localPort);
            do {
                new ProxyThread(proxyServer.accept());
            } while(true);
        } catch (IOException err){

        }

    }

}
