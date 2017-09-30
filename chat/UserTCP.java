/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho_1.chat;

import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 *
 * @author Avell B155 MAX
 */
public class UserTCP extends User {

    private BufferedReader reader;
    private PrintWriter writer;

    public UserTCP(BufferedReader reader, PrintWriter writer, String name) {
        super(name);
        this.writer = writer;
        this.reader = reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setReader(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     *
     * @param msg mensagem a ser recebida por este usuário TCP
     */
    @Override
    public void getMessage(String msg) {
        this.writer.println(msg);
    }
}
