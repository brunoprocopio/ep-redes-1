package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;

public class MainServer {

    private static final int PORT = 3000;
    private static HashMap<String, User> users;
    private static final String FILES_PATH = "D:\\Dev\\EACH\\ep\\";

    public static void main(String[] args) throws Exception {
        users = new HashMap<>();
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();

            // Cria o reader e writer
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Executa o comando recebido
            executeCommand(out, in);

            // Fecha conexão
            out.close();
            in.close();
        }
    }

    private static void executeCommand(PrintWriter out, BufferedReader in) {
        try {
            String commandRcv = in.readLine();
            Command command = getCommand(commandRcv);

            System.out.println("Recebido: " + command.getAction());

            switch (command.getAction()) {
                case "LOGIN":
                    if (command.getArgs()[0].equals("admin")) {
                        if (loginAdmin(command.getArgs()[1])) {
                            out.println("LOGADO");
                        } else {
                            out.println("ERRO");
                        }
                    } else {
                        if (login(command.getArgs()[0])) {
                            out.println("LOGADO");
                        } else {
                            out.println("ERRO");
                        }
                    }
                    break;
                case "FILE":
                    out.println(receiveFile(command.getArgs()[0], command.getArgs()[1], command.getArgs()[2]));
                    break;
                case "LIST_FILES":
                    out.println(listFiles(command.getArgs()[0]));
                case "DELETE_FILE":
                    out.println(deleteFile(command.getArgs()[0], command.getArgs()[1]));
                case "DOWNLOAD_FILE":
                    out.println(sendFile(command.getArgs()[0], command.getArgs()[1]));
                case "LOGOUT":
                    logout(command.getArgs()[0]);
                case "LIST_USERS":
                    out.println(listUsers());
                case "LIST_USER_FILES":
                    out.println(listUserFiles(command.getArgs()[0]));
                default:
                    out.println("INVALID");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Command getCommand(String command) {
        return new Command(command.split("[|]"));
    }

    private static boolean loginAdmin(String password) {
        return password.equals("admin");
    }

    private static boolean login(String user) {
        if (users.containsKey(user)) {
            if (users.get(user).loggedIn())
                return false;
        } else {
            users.put(user, new User(user));
        }

        users.get(user).login();
        return true;
    }

    private static String receiveFile(String user, String fileName, String base64) throws Exception {
        // checar se arquivo já existe
        String filePath = FILES_PATH + "\\" + user + "\\" + fileName;
        ServerFile file = new ServerFile(fileName, filePath);

        if (users.get(user).hasFile(file)) {
            return "FILE_EXISTS";
        } else {
            createUserDirectory(user);
            byte[] imageByteArray = decodeImage(base64);
            FileOutputStream imageOutFile = new FileOutputStream(file.getPath());
            imageOutFile.write(imageByteArray);
            imageOutFile.close();

            users.get(user).addFile(file);
        }
        return "OK";
    }

    private static String listFiles(String user) {
        return users.get(user).getFiles();
    }

    private static String deleteFile(String user, String fileName) {
        ServerFile file = new ServerFile(fileName);

        if (!users.get(user).hasFile(file))
            return "FILE_DOESNT_EXIST";

        users.get(user).deleteFile(file);
        return "OK";
    }

    private static String sendFile(String user, String fileName) throws Exception {
        if (!users.get(user).hasFile(new ServerFile(fileName)))
            return "FILE_DOESNT_EXIST";

        File file = new File(users.get(user).getFile(fileName).getPath());
        FileInputStream imageInFile = new FileInputStream(file);
        byte imageData[] = new byte[(int) file.length()];
        imageInFile.read(imageData);

        String imageDataString = encodeImage(imageData);
        imageInFile.close();

        return imageDataString;
    }

    private static void logout(String user) {
        users.get(user).logout();
    }

    private static String listUsers() {
        String response = "";
        for (String user : users.keySet()) {
            System.out.println(user);
            response += user + "|";
        }
        return response;
    }

    private static String listUserFiles(String user) {
        if (!users.containsKey(user))
            return "INVALID_USER";
        return users.get(user).getFiles();
    }

    private static byte[] decodeImage(String imageDataString) {
        return Base64.getDecoder().decode(imageDataString);
    }

    private static String encodeImage(byte[] imageByteArray) {
        return Base64.getEncoder().encodeToString(imageByteArray);
    }

    private static void createUserDirectory(String user) {
        File dir = new File(FILES_PATH + "\\" + user);
        if (!dir.exists())
            dir.mkdirs();
    }
}