package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;

public class MainServer {

    // variável que armazena a porta que o client vai se conectar com o server
    private static final int PORT = 3000;

    // coleção para armazenar os usuários logados
    private static HashMap<String, User> users;

    // variável que armazena o caminho da máquina onde os arquivos serão salvos
    private static final String FILES_PATH = "D:\\Dev\\EACH\\ep\\";

    // como referência para o socket, foi utilizado esse exemplo
    // https://www.baeldung.com/a-guide-to-java-sockets
    public static void main(String[] args) throws Exception {
        users = new HashMap<>();
        ServerSocket serverSocket = new ServerSocket(PORT);

        // aqui fazemos um loop que roda para sempre, sempre escutando a porta e
        // executando o comando recebido
        while (true) {
            // Aceita a conexão
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

    // Método que executa o comando recebido
    private static void executeCommand(PrintWriter out, BufferedReader in) {
        try {
            String commandRcv = in.readLine();
            // transforma o comando recebido (string) em um objeto do tipo Command
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

    // método resposável pelo login do usuário
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

    // método responsável por receber um upload de arquivo
    private static String receiveFile(String user, String fileName, String base64) throws Exception {
        // checar se arquivo já existe
        String filePath = FILES_PATH + "\\" + user + "\\" + fileName;
        ServerFile file = new ServerFile(fileName, filePath);

        if (users.get(user).hasFile(file)) {
            return "FILE_EXISTS";
        } else {
            createUserDirectory(user);

            // faz a conversão do base64
            byte[] imageByteArray = decodeImage(base64);
            FileOutputStream imageOutFile = new FileOutputStream(file.getPath());
            imageOutFile.write(imageByteArray);
            imageOutFile.close();

            users.get(user).addFile(file);
        }
        return "OK";
    }

    // método responsável por listar os arquivos de um usuário
    private static String listFiles(String user) {
        return users.get(user).getFiles();
    }

    // método responsável por apagar um arquivo do servidor
    private static String deleteFile(String user, String fileName) {
        ServerFile file = new ServerFile(fileName);

        if (!users.get(user).hasFile(file))
            return "FILE_DOESNT_EXIST";

        users.get(user).deleteFile(file);
        return "OK";
    }

    // método responsável por enviar um arquivo para o cliente
    // lê o arquivo do disco e o envia como uma string em base64 
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

    // método que faz o logout de um usuário
    private static void logout(String user) {
        users.get(user).logout();
    }

    // método para listar os usários que se logaram no servidor
    private static String listUsers() {
        String response = "";
        for (String user : users.keySet()) {
            System.out.println(user);
            response += user + "|";
        }
        return response;
    }

    // método para listar os arquivos de um determinado usuário
    private static String listUserFiles(String user) {
        if (!users.containsKey(user))
            return "INVALID_USER";
        return users.get(user).getFiles();
    }

    // transforma imagem de string (base64) para um array de bytes
    // para ser escrito no disco
    private static byte[] decodeImage(String imageDataString) {
        return Base64.getDecoder().decode(imageDataString);
    }

    // exemplo pego de https://giuliascalaberni.wordpress.com/2017/01/19/transfer-images-with-java-socket/
    // transforma o arquivo (em bytes) para uma string base64
    private static String encodeImage(byte[] imageByteArray) {
        return Base64.getEncoder().encodeToString(imageByteArray);
    }

    // exemplo pego de https://giuliascalaberni.wordpress.com/2017/01/19/transfer-images-with-java-socket/
    // cria um dirétorio dentro de FILES_PATH para o usuário, caso esse 
    // diretório não exista
    private static void createUserDirectory(String user) {
        File dir = new File(FILES_PATH + "\\" + user);
        if (!dir.exists())
            dir.mkdirs();
    }
}
