
import javacard.framework.ISO7816;
// import javax.smartcardio.CardChannel;
// import javax.smartcardio.CommandAPU;
import javax.smartcardio.CardException;
import javax.smartcardio.ResponseAPDU;

import im.status.keycard.desktop.PCSCCardChannel;
import im.status.keycard.globalplatform.GlobalPlatformCommandSet;
import im.status.keycard.globalplatform.LoadCallback;
import im.status.keycard.io.APDUException;
import im.status.keycard.io.CardChannel;
import im.status.keycard.io.APDUCommand;
import im.status.keycard.io.APDUResponse;

import javax.smartcardio.*;

import java.io.FileInputStream;
import java.io.IOException;

import org.bouncycastle.util.encoders.Hex;

import org.junit.Assert;

public class App
{
    static GlobalPlatformCommandSet cmdSet;
    public static void main(String args[]) 
    {
        System.out.println("---begin---");
        TerminalFactory tf = TerminalFactory.getDefault();
        CardTerminal cardTerminal = null;

        im.status.keycard.io.CardChannel apduChannel;

        try {
            for (CardTerminal t : tf.terminals().list()) {
                if (t.isCardPresent()) {
                    cardTerminal = t;
                    break;
                }
            }
        } catch (CardException e) {
            System.out.println("Error listing card terminals");
        }

        if (cardTerminal == null) {
            System.out.println("No available PC/SC terminal");
            return;
        }

        Card apduCard;

        try {
            apduCard = cardTerminal.connect("*");
        } catch (CardException e) {
            System.out.println("Couldn't connect to the card");
            return;
        }

        System.out.println("Connected to " + cardTerminal.getName());
        PCSCCardChannel sdkChannel
            = new PCSCCardChannel(apduCard.getBasicChannel());
        cmdSet = new GlobalPlatformCommandSet(sdkChannel);

        try {
            cmdSet.select().checkOK();
            cmdSet.select_auth().checkOK();
            cmdSet.openSecureChannel();

            test_processAddPersona();
            getSL();
            //Thread.sleep(3000);

            test_processAddVerifierForPersona();
        } catch (IOException e) {
            System.out.println("I/O error");
        } catch (APDUException e) {
            System.out.println("APDUException ");
        } 

        System.out.println("---end---");
    }

    public static void getSL() throws IOException
    {
        APDUCommand c22 = new APDUCommand(
            ISO7816.CLA_ISO7816, 0x1B, 0x00, 0x00, new byte[0]);

        APDUResponse r22 = cmdSet.secureChannel.send(c22);
        if (r22.getSw() == 0x9000) {
            System.out.println("SL CHECK:");
            prettyOut(r22.getData());
        } else {
            System.out.println("SL CHECK FAILED" + r22.getSw());
        }
    }

    public static void test_processAddVerifierForPersona() throws IOException
    {
        byte[] data = Hex.decode(
            "8200910210007F2E868184268B8129A7402DAC91335793342B8437814237C24238D34238E0423EEE423F4F43433F44521A45662D956D664470745379F2527DE64286EF42905B8697939297A0919AF3929F8D94A2878FA3948FA4A250AB854CB0C651B8CF41B8DA51CAA050D03C4CD54D5DD7175BDBBB50E0255CE5415DE72C4CE7FE41F1B05EF2914EF9C880FC258B");

        APDUCommand c2
            = new APDUCommand(ISO7816.CLA_ISO7816, 0x2A, 0x00, 0x00, data);

        APDUResponse r2 = cmdSet.secureChannel.send(c2);
        if (r2.getSw() == 0x9000) {
            System.out.println("SUCCESS AVP");
        } else {
            System.out.println("FAILED AVP = " + r2.getSw());
            prettyOut(r2.getData());
        }
    }

    public static void test_processAuthenticatePersona() throws IOException
    {
        byte[] data = Hex.decode(
            "7F2E868184268B8129A7402DAC91335793342B8437814237C24238D34238E0423EEE423F4F43433F44521A45662D956D664470745379F2527DE64286EF42905B8697939297A0919AF3929F8D94A2878FA3948FA4A250AB854CB0C651B8CF41B8DA51CAA050D03C4CD54D5DD7175BDBBB50E0255CE5415DE72C4CE7FE41F1B05EF2914EF9C880FC258B");

        APDUCommand c3
            = new APDUCommand(ISO7816.CLA_ISO7816, 0xEF, 0x1D, 0xCD, data);

        APDUResponse r3 = cmdSet.secureChannel.send(c3);
        if (r3.getSw() == 0x9000) {
            System.out.println("SUCCESS AUP");
        } else {
            System.out.println("FAILED AUP = " + r3.getSw());
        }
    }

    public static void test_processAddPersona() throws IOException
    {
        APDUCommand c = new APDUCommand(
            ISO7816.CLA_ISO7816, 0x1A, 0x00, 0x00, new byte[0]);

        APDUResponse r = cmdSet.secureChannel.send(c);

        if (r.getSw() == 0x9000) {
            System.out.println("SUCCESS AP");
        } else {
            System.out.println("FAILED AP = " + r.getSw());
        }
    }

    public static void prettyOut(byte[] msg)
    {
        for (int j = 1; j < msg.length + 1; j++) {
            if (j % 8 == 1 || j == 0) {
                if (j != 0) {
                    System.out.println();
                }
                System.out.format("0%d\t|\t", j / 8);
            }
            System.out.format("%02X", msg[j - 1]);
            if (j % 4 == 0) {
                System.out.print(" ");
            }
        }
        System.out.println();
    }
}
