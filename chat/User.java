/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabalho_1.chat;

import trabalho_1.interfaces.IUser;

/**
 *
 * @author Avell B155 MAX
 */
public class User implements IUser {

    private String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void getMessage(String msg) {
        //DEVE SER IMPLEMENTADO NAS SUBCLASSES
    }

}
