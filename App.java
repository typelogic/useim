
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
        /* 
         => 04 2A 00 00 98 37 29 9C 8D EC 96 4C 3A 3D 7A 6B    .*...7)....L:=zk
            9F D6 8E C1 CE 64 64 8F 0E 67 F5 CC 90 25 0D 53    .....dd..g...%.S
            A7 E7 6B 9D 11 09 7F 55 87 8F E8 11 63 A5 33 A3    ..k....U....c.3.
            5B 0F E7 46 1E AF 12 DD 8E 14 76 5C 31 16 4F CC    [..F......v\1.O.
            B2 D0 92 9F 8D D8 EB E4 5F C2 E5 D4 39 80 84 ED    ........_...9...
            7F 71 14 4A C9 14 E8 FB 55 CB 3B 26 09 9A 34 6B    .q.J....U.;&..4k
            28 09 CB 63 A2 C5 23 76 0A 5F 42 6E C0 F0 6E 36    (..c..#v._Bn..n6
            6C ED 78 37 8D 28 44 77 51 0C 24 53 F7 86 B1 C3    l.x7.(DwQ.$S....
            6A 52 63 4D D3 08 AA 0F BF F4 F3 0A 24 1D 04 38    jRcM........$..8
            EB 8C 1E 2A 25 E5 34 7B 59 86 5A 8B 62             ...*%.4{Y.Z.b
        */

        /*
        // this one should also be ok
        byte[] data = Hex.decode(
            "8200910210007F2E868184268B8129A7402DAC91335793342B8437814237C24238D34238E0423EEE423F4F43433F44521A45662D956D664470745379F2527DE64286EF42905B8697939297A0919AF3929F8D94A2878FA3948FA4A250AB854CB0C651B8CF41B8DA51CAA050D03C4CD54D5DD7175BDBBB50E0255CE5415DE72C4CE7FE41F1B05EF2914EF9C880FC258B");
        */

        // same to above data that uses Hex.decode() 
        byte data[] = {
            (byte)0x82,(byte)0x00,(byte)0x91,(byte)0x02,(byte)0x10,(byte)0x00,
            (byte)0x7F,(byte)0x2E,(byte)0x86,(byte)0x81,(byte)0x84,(byte)0x26,
            (byte)0x8B,(byte)0x81,(byte)0x29,(byte)0xA7,(byte)0x40,(byte)0x2D,
            (byte)0xAC,(byte)0x91,(byte)0x33,(byte)0x57,(byte)0x93,(byte)0x34,
            (byte)0x2B,(byte)0x84,(byte)0x37,(byte)0x81,(byte)0x42,(byte)0x37,
            (byte)0xC2,(byte)0x42,(byte)0x38,(byte)0xD3,(byte)0x42,(byte)0x38,
            (byte)0xE0,(byte)0x42,(byte)0x3E,(byte)0xEE,(byte)0x42,(byte)0x3F,
            (byte)0x4F,(byte)0x43,(byte)0x43,(byte)0x3F,(byte)0x44,(byte)0x52,
            (byte)0x1A,(byte)0x45,(byte)0x66,(byte)0x2D,(byte)0x95,(byte)0x6D,
            (byte)0x66,(byte)0x44,(byte)0x70,(byte)0x74,(byte)0x53,(byte)0x79,
            (byte)0xF2,(byte)0x52,(byte)0x7D,(byte)0xE6,(byte)0x42,(byte)0x86,
            (byte)0xEF,(byte)0x42,(byte)0x90,(byte)0x5B,(byte)0x86,(byte)0x97,
            (byte)0x93,(byte)0x92,(byte)0x97,(byte)0xA0,(byte)0x91,(byte)0x9A,
            (byte)0xF3,(byte)0x92,(byte)0x9F,(byte)0x8D,(byte)0x94,(byte)0xA2,
            (byte)0x87,(byte)0x8F,(byte)0xA3,(byte)0x94,(byte)0x8F,(byte)0xA4,
            (byte)0xA2,(byte)0x50,(byte)0xAB,(byte)0x85,(byte)0x4C,(byte)0xB0,
            (byte)0xC6,(byte)0x51,(byte)0xB8,(byte)0xCF,(byte)0x41,(byte)0xB8,
            (byte)0xDA,(byte)0x51,(byte)0xCA,(byte)0xA0,(byte)0x50,(byte)0xD0,
            (byte)0x3C,(byte)0x4C,(byte)0xD5,(byte)0x4D,(byte)0x5D,(byte)0xD7,
            (byte)0x17,(byte)0x5B,(byte)0xDB,(byte)0xBB,(byte)0x50,(byte)0xE0,
            (byte)0x25,(byte)0x5C,(byte)0xE5,(byte)0x41,(byte)0x5D,(byte)0xE7,
            (byte)0x2C,(byte)0x4C,(byte)0xE7,(byte)0xFE,(byte)0x41,(byte)0xF1,
            (byte)0xB0,(byte)0x5E,(byte)0xF2,(byte)0x91,(byte)0x4E,(byte)0xF9,
            (byte)0xC8,(byte)0x80,(byte)0xFC,(byte)0x25,(byte)0x8B};
        
        System.out.println("LEN = " + data.length);

        // /send "00 2A 00 00 #(${verifierTemplateData})"
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
