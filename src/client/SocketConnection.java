package client;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class SocketConnection {

    public PrintWriter out;
    public BufferedReader in;

    public SocketConnection(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }
}
