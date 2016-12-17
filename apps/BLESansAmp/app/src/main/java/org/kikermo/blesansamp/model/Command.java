package org.kikermo.blesansamp.model;

/**
 * Created by EnriqueR on 17/12/2016.
 */

public class Command {
    int cmd;
    int act;
    int value;

    public Command() {
    }

    public Command(byte[] command) {
        if (command.length < 3)
            return;
        cmd = command[0];
        act = command[1];
        value = command[2];
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getAct() {
        return act;
    }

    public void setAct(int act) {
        this.act = act;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public byte[] toByteArray() {
        return new byte[]{(byte) cmd, (byte) act, (byte) value, '\n'};
    }

}
