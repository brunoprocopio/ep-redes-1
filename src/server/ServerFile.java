package server;

import java.util.Objects;

public class ServerFile {

    private String name;
    private String path;

    public ServerFile(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public ServerFile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerFile that = (ServerFile) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
