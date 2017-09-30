/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho_1.chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Avell B155 MAX
 */
public class UserUDP extends User {

    private DatagramPacket datagramtPacket;
    private DatagramSocket aSocket;

    public UserUDP(DatagramPacket datagramPacket, DatagramSocket aSocket, String name) {
        super(name);
        this.datagramtPacket = datagramPacket;
        this.aSocket = aSocket;
    }

    public DatagramPacket getDatagramPacket() {
        return datagramtPacket;
    }

    /**
     *
     * @param msg mensagem a ser recebida por este usuário UDP
     */
    @Override
    public void getMessage(String msg) {
        datagramtPacket = new DatagramPacket(msg.getBytes(), msg.length(), datagramtPacket.getAddress(), datagramtPacket.getPort());
        try {
            //datagramtPacket.setData(msg.getBytes());
            aSocket.send(datagramtPacket);
        } catch (IOException ex) {
            Logger.getLogger(UserUDP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
