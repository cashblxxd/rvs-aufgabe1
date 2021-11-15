import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Diese Klasse enthält Methoden zur Verarbeitung von Streams
 *
 * @author RvS-Team 2021/2022
 */
public class Streams {
    /**
     * Schreibt das übergebene <code>byte</code>-array in den {@link OutputStream}
     * @param data Daten zum schreiben
     * @param os Ziel
     * @return <code>true</code> bei Erfolg, sonst <code>false</code>
     */
    public boolean writeArrayToStream(byte[] data, OutputStream os) {
        try {
            os.write(data);
        } catch (IOException ioException) {
            return false;
        }
        return true;
    }

    /**
     * Schreibt das übergebene <code>byte</code>-array ab <code>offset</code> für <code>bytes</code>-<code>Länge</code>
     * in den {@link OutputStream}
     * @param data Daten zum schreiben
     * @param offset Start der zu schreibenden Bytes
     * @param length Anzahl der zu schreibenden Bytes
     * @param os Ziel
     * @return <code>true</code> bei Erfolg, sonst <code>false</code>
     */
    public boolean writeArrayToStream(byte[] data, int offset, int length, OutputStream os) {
        for(int i = offset; i < offset + length; ++i)
            try {
                os.write(data[i]);
            } catch (IOException ioException) {
                return false;
            }
        return true;
    }

    /**
     * Liest solange aus dem {@link InputStream} und kopiert/schreibt die Daten in den {@link OutputStream}, bis das
     * Ende des {@link InputStream}s erreicht wurde. Die Nutzung eines Puffers ist empfohlen.
     * @param is Daten zum schreiben
     * @param os Ziel
     * @return <code>true</code> bei Erfolg, sonst <code>false</code>
     * @implNote Ein InputStream gibt <code>-1</code> bei einer {@link InputStream#read()} zurück, wenn das Ende erreicht wurde
     */
    public boolean writeInputToOutput(InputStream is, OutputStream os) {
        int size = 16384, n;
        byte[] puffer = new byte[size];
        try {
            n = is.readNBytes(puffer, 0, size);
        } catch (IOException ioException) {
            return false;
        }
        while (n != 0) {
            try {
                os.write(puffer, 0, n);
                n = is.readNBytes(puffer, 0, size);
            } catch (IOException ioException) {
                return false;
            }
        }
        return true;
    }

    /**
     * Schreibt den übergebenen {@link String} in der <code>UTF-8</code>-Kodierung in den {@link OutputStream}.
     * @param text Daten zum schreiben
     * @param os Ziel
     * @return <code>true</code> bei Erfolg, sonst <code>false</code>
     * @see StandardCharsets#UTF_8
     */
    public boolean writeUTF8TextToStream(String text, OutputStream os) {
        try {
            os.write(text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ioException) {
            return false;
        }
        return true;
    }

    /**
     * Schreibt den übergebenen {@link String} in der <code>UTF-8</code>-Kodierung in den {@link OutputStream}.
     * @param text Daten zum schreiben
     * @param encoding Die zu nutzende Zeichenkodierung
     * @param os Ziel
     * @return <code>true</code> bei Erfolg, sonst <code>false</code>
     */
    public boolean writeEncodedTextToStream(String text, Charset encoding, OutputStream os) {
        try {
            os.write(text.getBytes(encoding));
        } catch (IOException ioException) {
            return false;
        }
        return true;
    }

    /**
     * Gibt einen {@link PrintStream} zurück, der in den übergebenen {@link OutputStream} schreibt.
     * @param os Ziel
     * @return den erzeugten {@link PrintStream}
     */
    public PrintStream asPrintStream(OutputStream os) {
        return new PrintStream(os);
    }

    /**
     * Schreibt erst den übergebenen {@link String} mit einem Zeilenumbruch in den {@link PrintStream} und hängt danach die Daten, wie in {@link #writeInputToOutput(InputStream, OutputStream)}, an.
     * @param os Ziel
     * @param text Zu schreibender Text
     * @param is zu übertragender {@link InputStream}
     * @return <code>true</code> bei Erfolg, sonst <code>false</code>
     * @see PrintStream#checkError()
     */
    public boolean writeToPrintStream(PrintStream os, String text, InputStream is) {
        os.println(text);
        int size = 16384, n;
        byte[] puffer = new byte[size];
        try {
            n = is.readNBytes(puffer, 0, size);
        } catch (IOException ioException) {
            return false;
        }
        while (n != 0) {
            try {
                os.write(puffer, 0, n);
                n = is.readNBytes(puffer, 0, n);
            } catch (IOException ioException) {
                return false;
            }
        }
        return true;
    }

    /**
     * List aus dem übergebenen {@link String} den Wert von <code>param2</code>. Wenn <code>param2</code> nicht gegeben
     * ist oder ein Fehler auftritt, so soll {@link Double#NaN} zurückgegeben werden. Der übergebene String enthält
     * dabei Text nach diesen Beispielen: <br>
     * <code>command --param1 text --param2 123.456 --param3 asdf</code><br>
     * <code>andererCommand --param2 123.456</code><br>
     * <code>command --param3 Einhorn</code><br>
     * <code></code> (leerer String)<br>
     * @param text Zu parsender Text
     * @return Den ausgelesenen Wert. Oder {@link Double#NaN}, wenn dieser nicht existert oder ein Fehler aufgetreten ist.
     */
    public double parseParam2(String text) {
        String[] tokens = text.split("\\s+");
        for(int i = 0; i < tokens.length; ++i) {
            if(tokens[i].equals("--param2"))
                return Double.parseDouble(tokens[i + 1]);
        }
        return Double.NaN;
    }

    /**
     * Gibt einen {@link BufferedReader} zurück, der aus dem übergebenen {@link InputStream} liest.
     * @param is Quelle
     * @return den erzeugten {@link BufferedReader}
     */
    public BufferedReader asBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Der übergebene {@link BufferedReader} enthält pro Zeile eine Zahl (sprich ein oder mehrere Ziffern). Diese Zahlen
     * sollen aufsummiert werden und am Ende der Daten zurückgegeben werden. Das Ende der Daten wird durch die Zeile
     * <code>end</code> markiert. <br>
     * <code>1234</code><br>
     * <code>4567</code><br>
     * <code>8901</code><br>
     * <code>2345</code><br>
     * <code>end</code><br>
     * @param br Eingabe
     * @return Die Summe der gelesenen Werte. <code>0</code> bei auftreten eines Fehlers
     * @implNote Achten Sie darauf, dass die Methode keine {@link Exception} wirft, falls eine Zeile mal etwas anderes als eine Zahl oder den Text <code>end</code> enthält.
     */
    public long sum(BufferedReader br) {
        long res = 0;
        try {
            for(String line; (line = br.readLine()) != null;) {
                if(line.equals("end"))
                    return res;
                res += Long.parseLong(line);
            }
        } catch (IOException ioException) {
            return res;
        }
        return res;
    }

    /**
     * Der übergebene {@link BufferedReader} enthält pro Zeile einen Text in dem eine Zahl (sprich ein oder mehrere Ziffern)
	 * enthalten ist. Diese Zahlen sollen als Liste in der gegebenen Reihenfolge zuückgegeben werden Das Ende der Daten 
	 * wird durch die Zeile <code>end</code> markiert. <br>
     * <code>Es liegen 13 Früchte im Korb.</code><br>
     * <code>Es liegen 3 Früchte im Korb.</code><br>
     * <code>Es liegen 5 Früchte im Korb.</code><br>
     * <code>Es liegen n Früchte im Korb.</code><br>
     * <code>end</code><br>
     * @param br Eingabe
     * @return Die Liste der gelesenen Werte. <code>null</code> bei einem Fehler.
     * @see java.util.ArrayList
     */
    public List<Integer> getIntList(BufferedReader br) {
        List<Integer> res = new ArrayList<>();
        try {
            for(String line; (line = br.readLine()) != null;) {
                if(line.equals("end"))
                    return res;
                for(String i : line.split("\\s+"))
                    try {
                        res.add(Integer.parseInt(i));
                    } catch (NumberFormatException ignored) {} // no better way to check if it's an integer (.isalnum()-alike)
            }
        } catch (IOException ioException) {
            return res;
        }
        return res;
    }
}
