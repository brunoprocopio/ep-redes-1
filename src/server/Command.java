package server;

// Classe para abstrair o comando em string para um objeto
public class Command {
    private String action;
    private String[] args;

    // Aqui fazemos o parse da string e salvamos os parâmetros recebidos
    // e o comando recebido
    public Command(String[] values) {
        this.action = values[0];
        this.args = new String[values.length - 1];

        for (int i = 1; i < values.length; i++){
            this.args[i - 1] = values[i];
        }
    }

    public String getAction() {
        return action;
    }

    public String[] getArgs() {
        return this.args;
    }
}
