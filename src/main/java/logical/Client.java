package logical;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    private Socket socket = null;
    private DataOutputStream output = null;
    private DataInputStream input = null;

    public Client(String address, int port) {
        try {
            this.socket = new Socket(address, port);
            this.output = new DataOutputStream(socket.getOutputStream());
            this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch(UnknownHostException e) {
            System.out.println("Server " + address + " on port " + port + " is down!\n" + e);
            System.exit(-1);
        } catch(IOException e) {
            System.out.println("Server " + address + " on port " + port + " is down!\n" + e);
            System.exit(-1);
        }
    }

    public void write(String message) {
        if (socket != null) {
            try {
                output.write(message.getBytes());
            } catch(IOException i) {
                System.out.println(i);
            }
        }
    }

    public String readLine() {
        String result = "";
        if (socket != null) {
            try {
                BufferedReader stream = new BufferedReader(new InputStreamReader(input));
                result = stream.readLine();
            } catch(IOException i) {
                System.out.println(i);
            }
        }
        return result;
    }
}
