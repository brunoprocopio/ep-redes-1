package server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Classe para abstrair a entidade usuário
public class User {

    // variável que armazena o login do usuário
    private String user;

    // variável para armazenar os arquivos do usuário
    private Set<ServerFile> files;

    // variável que armazena se o usuário já fez o logout
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
            filesString += (it.next().getName() + " | ");
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
