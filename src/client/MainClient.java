package client;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class MainClient {
    public static String USER;

    private static Scanner scanner = new Scanner(System.in);

    private static final int PORT = 3000;

    public static void main(String[] args) throws Exception {
        String login = printLogin();
        USER = login;

        if (login.equals("admin")) {
            String password = printPassword();
            String loginResponse = sendCommandToServer("LOGIN|" + login + "|" + password);

            if (loginResponse.equals("LOGADO")) {
                startAdminLoop();
            } else {
                System.out.println("Senha incorreta!");
            }
        } else {
            String loginResponse = sendCommandToServer("LOGIN|" + login);
            if (loginResponse.equals("LOGADO")) {
                startLoop();
            } else {
                System.out.println("Usuário já está logado!");
            }
        }
    }

    private static String sendCommandToServer(String command) throws IOException {
        SocketConnection conn = getConnection();
        conn.out.println(command);
        String response = conn.in.readLine();
        closeConnection(conn);
        return response;
    }

    private static void closeConnection(SocketConnection conn) throws IOException {
        conn.out.close();
        conn.in.close();
    }

    public static SocketConnection getConnection() throws IOException {
        Socket socket = new Socket("localhost", PORT);

        // Cria o reader e writer
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        return new SocketConnection(out, in);
    }

    private static String printLogin() {
        System.out.println("Digite o usuário para login:");
        return scanner.nextLine();
    }

    private static String printPassword() {
        System.out.println("Digite a senha:");
        return scanner.nextLine();
    }

    private static void startLoop() throws Exception {
        boolean running = true;

        while (running) {
            switch (getOption()) {
                case 1:
                    sendFile();
                    break;
                case 2:
                    listFiles();
                    break;
                case 3:
                    deleteFile();
                    break;
                case 4:
                    downloadFile();
                    break;
                default:
                    running = false;
                    logout();
                    break;
            }
        }
    }

    private static void startAdminLoop() throws Exception {
        boolean running = true;

        while (running) {
            switch (getAdminOption()) {
                case 1:
                    listUsers();
                    break;
                case 2:
                    listUserFiles();
                    break;
                default:
                    running = false;
                    break;
            }
        }
    }

    private static int getAdminOption() {
        System.out.println("Digite o comando desejado:");
        System.out.println("1 - Listar usuários");
        System.out.println("2 - Listar arquivos de usuário");
        System.out.println("0 - Sair");
        return Integer.parseInt(scanner.nextLine());
    }

    private static int getOption() {
        System.out.println("Digite o comando desejado:");
        System.out.println("1 - Enviar uma imagem");
        System.out.println("2 - Listar imagens enviadas");
        System.out.println("3 - Excluir uma imagem");
        System.out.println("4 - Download de arquivo");
        System.out.println("0 - Sair");
        return Integer.parseInt(scanner.nextLine());
    }

    private static void sendFile() {
        String fileName = "";
        String filePath = "";

        System.out.println("=== ENVIAR IMAGEM ===");
        System.out.println("Qual o nome do arquivo?");
        fileName = scanner.nextLine();
        System.out.println("Qual o caminho do arquivo?");
        filePath = scanner.nextLine();

        try {
            File file = new File(filePath);

            FileInputStream imageInFile = new FileInputStream(file);
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);

            String imageDataString = encodeImage(imageData);
            imageInFile.close();

            String response = sendCommandToServer("FILE|" + USER + "|" + fileName + "|" + imageDataString);

            switch (response) {
                case "OK":
                    System.out.println("Arquivo enviado com sucesso");
                    break;
                case "FILE_EXISTS":
                    System.out.println("Arquivo já existe!");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void listFiles() throws IOException {
        String response = sendCommandToServer("LIST_FILES|" + USER);
        String[] files = response.split("[|]");
        for (String file : files) {
            System.out.println(file);
        }
    }

    private static void deleteFile() throws Exception {
        String fileName = "";

        System.out.println("=== DELETAR IMAGEM ===");
        System.out.println("Qual o nome do arquivo?");
        fileName = scanner.nextLine();

        String response = sendCommandToServer("DELETE_FILE|" + USER + "|" + fileName);

        switch (response) {
            case "OK":
                System.out.println("Arquivo deletado com sucesso!");
                break;
            case "FILE_DOESNT_EXIST":
                System.out.println("Arquivo não existe!");
                break;
        }
    }

    private static void downloadFile() throws Exception {
        String fileName = "";
        String filePath = "";

        System.out.println("=== DOWNLOAD DE IMAGEM ===");
        System.out.println("Qual o nome do arquivo?");
        fileName = scanner.nextLine();
        System.out.println("Onde o arquivo vai ser salvo?");
        filePath = scanner.nextLine();

        String response = sendCommandToServer("DOWNLOAD_FILE|" + USER + "|" + fileName);

        switch (response) {
            case "FILE_DOESNT_EXIST":
                System.out.println("Arquivo não existe!");
                break;
            default:
                byte[] imageByteArray = decodeImage(response);
                FileOutputStream imageOutFile = new FileOutputStream(filePath);
                imageOutFile.write(imageByteArray);
                imageOutFile.close();
                System.out.println("Arquivo baixado com sucesso!");
                break;
        }
    }

    private static void logout() throws Exception {
        sendCommandToServer("LOGOUT|" + USER);
        System.out.println("Tchau!");
    }

    private static void listUsers() throws Exception {
        String response = sendCommandToServer("LIST_USERS");
        String[] users = response.split("[|]");
        for (String user : users) {
            System.out.println(user);
        }
    }

    private static void listUserFiles() throws Exception {
        System.out.println("Qual o usuário?");
        String user = scanner.nextLine();

        String response = sendCommandToServer("LIST_USER_FILES|" + user);
        if (response.equals("INVALID_USER")) {
            System.out.println("Usuário inválido!");
        } else {
            String[] files = response.split("[|]");
            for (String file : files) {
                System.out.println(file);
            }
        }
    }

    private static byte[] decodeImage(String imageDataString) {
        return Base64.getDecoder().decode(imageDataString);
    }

    private static String encodeImage(byte[] imageByteArray) {
        return Base64.getEncoder().encodeToString(imageByteArray);
    }

//    private static void showImage(BufferedImage image) {
//        ImageIcon imageIcon = new ImageIcon(image);
//        JLabel jlabel = new JLabel(imageIcon);
//
//        JPanel painel = new JPanel();
//        painel.add(jlabel);
//
//        JFrame janela = new JFrame("file");
//
//        janela.add(painel);
//        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        janela.pack();
//        janela.setVisible(true);
//    }

}