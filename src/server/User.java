package server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class User {

    private String user;
    private Set<ServerFile> files;
    private boolean loggedId;

    public User(String user) {
        this.user = user;
        this.files = new HashSet<>();
    }

    public void login() {
        this.loggedId = true;
    }

    public void logout() {
        this.loggedId = false;
    }

    public boolean loggedIn() {
        return this.loggedId;
    }

    public boolean hasFile(ServerFile file) {
        return this.files.contains(file);
    }

    public void addFile(ServerFile file) {
        this.files.add(file);
    }

    public String getFiles() {
        String filesString = "";
        Iterator<ServerFile> it = files.iterator();

        while (it.hasNext()) {
            filesString += (it.next().getName() + "|");
        }

        return filesString;
    }

    public ServerFile getFile(String filename) {
        ServerFile file = null;
        Iterator<ServerFile> it = files.iterator();

        while (it.hasNext()) {
            ServerFile f = it.next();
            if (f.equals(new ServerFile(filename))){
                file = f;
                break;
            }
        }

        return file;
    }

    public void deleteFile(ServerFile file) {
        files.remove(file);
    }
}
