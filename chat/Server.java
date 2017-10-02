/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho_1.chat;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Avell B155 MAX
 */
public class Server {

    private static int portTCP = 9001;
    private static int portUDP = 9002;
    private static HashSet<User> usuarios = new HashSet<User>();
    private static DatagramSocket aSocket = null;

    public static void main(String[] args) throws IOException {
        try {
            portTCP = Integer.parseInt(args[0]);
            portUDP = Integer.parseInt(args[1]);
        } catch (Exception ex) {
            portTCP = 9001;
            portUDP = 9002;
        }
        System.out.println("Servidor de Chat TCP inicializado como padrão: porta " + portTCP);
        System.out.println("Servidor de Chat UDP inicializado como padrão: porta " + portUDP);

        ServerSocket listener = new ServerSocket(portTCP);
        try {
            //UDP é apenas uma thread
            new HandleUDP().start();
            while (true) {
                //TCP Para cada cliente é criada uma thread
                new HandleTCP(listener.accept()).start();
            }
        } finally {
            listener.close();
        }

    }

    /**
     * Tratadores das mensagens
     *
     */
    private static class Handle extends Thread {

        protected User user;

        private void personalMessage(String msg) {
            msg = msg.substring(3);
            String nome = "";
            for (int i = 0; i < msg.length(); i++) {
                if (msg.charAt(i) != ' ') {
                    nome += msg.charAt(i);
                } else {
                    msg = msg.substring(i + 1);
                    break;
                }
            }
            for (User usuario : usuarios) {
                if (usuario.getName().equals(nome)) {
                    usuario.getMessage("MESSAGE FROM: <" + user.getName() + ">: " + msg);
                    user.getMessage("MESSAGE TO: <" + usuario.getName() + ">: " + msg);
                    return;
                }
            }
            user.getMessage("MESSAGE Usuario " + nome + " nao conectado");
        }

        private void broadcastMessage(String msg) {
            for (User cliente : usuarios) {
                cliente.getMessage("MESSAGE [" + user.getName() + "]: " + msg);
            }
        }

        private void logOut() {
            for (User cliente : usuarios) {
                if (cliente.getName().equals(user.getName())) {
                    cliente.getMessage("MESSAGE Voce saiu");
                } else {
                    cliente.getMessage("MESSAGE " + user.getName() + " saiu");
                }
            }
        }

        private void listUsers() {
            String s = "";
            for (User usuario : usuarios) {
                s += (usuario.getName() + ";");
            }
            if (!s.equals("")) {
                s = s.substring(0, s.length() - 1);
                for (User usuario : usuarios) {
                    //
                    usuario.getMessage("USERS " + s);
                }
            }
            //user.getWriter().println("USERS " + s);
        }
    }

    private static class HandleTCP extends Handle {

//        private User user;
        private Socket client;

        public HandleTCP(Socket client) {
            this.client = client;
            System.out.println(this.client.getInetAddress() + " conectado");
        }

        @Override
        public void run() {
            // quando chegar uma msg, distribui pra todos
            try {
                BufferedReader in;
                PrintWriter out;
                in = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
                out = new PrintWriter(this.client.getOutputStream(), true);

                //Lendo um NOME
                while (true) {
                    out.println("SUBMITNAME");
                    String name = in.readLine();

                    synchronized (usuarios) {
                        int cont = 0;
                        for (User usuario : usuarios) {
                            if (usuario.getName().equals(name)) {
                                cont = 1;
                                break;
                            }
                        }
                        if (cont == 0) {
                            usuarios.add(user = new UserTCP(in, out, name));
                            //names.add(name);
                            break;
                        }
                    }
                }
                user.getMessage("NAMEACCEPTED");
                super.listUsers();
                super.broadcastMessage(" se conectou");
                while (true) {
                    String msg = ((UserTCP) user).getReader().readLine();
                    if (msg == null) {
                        return;
                    }
                    if (msg.startsWith("/m")) {
                        super.personalMessage(msg);
                    } else if (msg.startsWith("/u")) {
                        super.listUsers();
                    } else if (msg.startsWith("/b")) {
                        super.logOut();
                        break;
                    } else {
                        super.broadcastMessage(msg);
                    }
                }
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
                super.logOut();
            } finally {
                for (User usuario : usuarios) {
                    if (user.getName().equals(usuario.getName())) {
                        //names.remove(user.getName());
                        usuarios.remove(usuario);
                        break;
                    }
                }
                try {
                    super.listUsers();
                    client.close();
                } catch (IOException e) {
                }
            }

        }

    }

    private static class HandleUDP extends Handle {

        private DatagramPacket request;

        @Override
        public void run() {
            try {
                aSocket = new DatagramSocket(portUDP);
                byte[] buffer = new byte[1024];
                String msg;
                while (true) {
                    Arrays.fill(buffer, (byte) 0);
                    request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    msg = new String(request.getData(), 0, request.getLength());
                    System.out.println(msg);
                    //Setando o usuario como o que acabou de enviar a mensagem
                    user = findUser();
                    //Análise da mensagem recebida
                    if (msg.startsWith("CONNECT")) {
                        //Solicitação de conexão
                        msg = msg.substring(8);
                        String resposta = ("NAMEACCEPTED");
                        for (User usuario : usuarios) {
                            if (usuario.getName().equals(msg)) {
                                resposta = ("SUBMITNAME");
                                break;
                            }
                        }
                        //Nome disponível e cadastrando na lista
                        if (resposta.equals("NAMEACCEPTED")) {
                            usuarios.add(user = new UserUDP(new DatagramPacket(resposta.getBytes(), 0, resposta.length(), request.getAddress(), request.getPort()), aSocket, msg));
                            user.getMessage(resposta);
                            super.listUsers();
                            super.broadcastMessage(" se conectou");
                        } else {
                            //Nome indisponível e enviando ao remetente a informação de que deve informar outro nickname
                            DatagramPacket dp = new DatagramPacket(resposta.getBytes(), 0, resposta.length(), request.getAddress(), request.getPort());
                            aSocket.send(dp);
                        }
                    } else if (msg.startsWith("/m")) {
                        //Mensagem privada
                        super.personalMessage(msg);
                    } else if (msg.startsWith("/u")) {
                        //caso solicite a atualizacao da lista de usuário
                        super.listUsers();
                    } else if (msg.startsWith("/b")) {
                        //solicitacao de desconexão
                        super.logOut();
                        usuarios.remove(findUser());
                        super.listUsers();
                    } else {
                        super.broadcastMessage(msg);
                    }
                }
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("IO: " + e.getMessage());
            } finally {
                if (aSocket != null) {
                    aSocket.close();
                }
            }
        }

        /**
         * Encontra o usuário UDP remetente da atual mensagem recebida através
         * de seu endereço e porta
         */
        private User findUser() {
            for (User usuario : usuarios) {
                if (usuario instanceof UserUDP) {
                    UserUDP u = (UserUDP) usuario;
                    if (u.getDatagramPacket() != null && u.getDatagramPacket().getAddress().equals(request.getAddress()) && u.getDatagramPacket().getPort() == (request.getPort())) {
                        return usuario;
                    }
                }

            }
            return null;
        }
    }

}
