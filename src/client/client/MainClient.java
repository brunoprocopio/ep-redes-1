package client;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class MainClient {
    
    // variável que armazena o nome do usuário quando logado 
    public static String USER;

    // variável para ler o input do usuário
    private static Scanner scanner = new Scanner(System.in);

    // variável para armazenar a porta de conexão com o servidor
    private static final int PORT = 3000;

    // no main é realizado o login e, dependendo do tipo de login (admin ou padrão) é 
    // direcionando para o loop de comandos do tipo de usuário
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

    // envia o comando do protocolo para o servidor 
    private static String sendCommandToServer(String command) throws IOException {
        SocketConnection conn = getConnection();
        conn.out.println(command);
        String response = conn.in.readLine();
        closeConnection(conn);
        return response;
    }

    // fecha a conexão com o socket
    private static void closeConnection(client.SocketConnection conn) throws IOException {
        conn.out.close();
        conn.in.close();
    }

    // cria conexão com o socket
    public static client.SocketConnection getConnection() throws IOException {
        Socket socket = new Socket("localhost", PORT);

        // Cria o reader e writer
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        return new client.SocketConnection(out, in);
    }

    // exibe mensagem para o login
    private static String printLogin() {
        System.out.println("Digite o usuário para login:");
        return scanner.nextLine();
    }

    // exibe mensagem para a senha (caso admin)
    private static String printPassword() {
        System.out.println("Digite a senha:");
        return scanner.nextLine();
    }

    // inicia loop do programa
    // exibe o menu e pega a opção digita pelo usuário
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

    // inicia loop do programa (admin)
    // exibe o menu e pega a opção digita pelo usuário
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

    // imprime o menu (admin) e lê a resposta
    private static int getAdminOption() {
        System.out.println("Digite o comando desejado:");
        System.out.println("1 - Listar usuários");
        System.out.println("2 - Listar arquivos de usuário");
        System.out.println("0 - Sair");
        return Integer.parseInt(scanner.nextLine());
    }

    // imprime o menu e lê a resposta
    private static int getOption() {
        System.out.println("Digite o comando desejado:");
        System.out.println("1 - Enviar uma imagem");
        System.out.println("2 - Listar imagens enviadas");
        System.out.println("3 - Excluir uma imagem");
        System.out.println("4 - Download de arquivo");
        System.out.println("0 - Sair");
        return Integer.parseInt(scanner.nextLine());
    }

    // método para enviar o arquivo para o servidor
    // opção 1 - Enviar uma imagem
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

            // aqui é feita a conversão do arquivo para base64
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

    // método para listar os arquivos de um usuário
    // opção 2 - Listar imagens enviadas
    private static void listFiles() throws IOException {
        String response = sendCommandToServer("LIST_FILES|" + USER);
        String[] files = response.split("[|]");
        for (String file : files) {
            System.out.println(file);
        }
    }

    // método para excluir um arquivo do servidor
    // opção 3 - Excluir uma imagem
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

    // método para fazer o download de um arquivo do servidor
    // opção 4 - Download de arquivo
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
                // aqui fazemos a conversão do arquivo recebido em base64 para poder escrever no disco
                byte[] imageByteArray = decodeImage(response);
                FileOutputStream imageOutFile = new FileOutputStream(filePath);
                imageOutFile.write(imageByteArray);
                imageOutFile.close();
                System.out.println("Arquivo baixado com sucesso!");
                break;
        }
    }

    // método para deslogar um usuário
    // apenas envia o comando para o servidor
    // opção 0 - Sair
    private static void logout() throws Exception {
        sendCommandToServer("LOGOUT|" + USER);
        System.out.println("Tchau!");
    }

    // ==== ADMIN ====
    // método para listar usuários do servidor
    // opção 1 - Listar usuários
    private static void listUsers() throws Exception {
        String response = sendCommandToServer("LIST_USERS");
        String[] users = response.split("[|]");
        for (String user : users) {
            System.out.println(user);
        }
    }

    // ==== ADMIN ====
    // método para listar arquivos de um determinado usuário
    // opção 2 - Listar arquivos de usuário
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

    // exemplo pego de https://giuliascalaberni.wordpress.com/2017/01/19/transfer-images-with-java-socket/
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
