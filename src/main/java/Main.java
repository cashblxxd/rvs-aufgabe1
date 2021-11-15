import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Diese Klasse enthält die Main-Methode, die einige einfache Tests durchführt.
 * Diese Klasse ist absichtlich nicht dokumentiert
 */
public class Main {
    public static void main(String[] args) {
        Streams s = new Streams();
        test_writeArrayToStreamA(s);
        test_writeArrayToStreamB(s);
        test_writeInputToOutput(s);
        test_writeUTF8TextToStream(s);
        test_writeEncodedTextToStream(s);
        test_asPrintStream(s);
        test_writeToPrintStream(s);
        test_parseParam2(s);
        test_asBufferedReader(s);
        test_sum(s);
        test_getIntList(s);
    }

    private static void test_writeArrayToStreamA(Streams s) {
        Random r = new Random();
        byte[] data = new byte[1024];
        r.nextBytes(data);
        byte[] backup = Arrays.copyOf(data, 1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean rv = s.writeArrayToStream(data, baos);
        System.out.println("writeArrayToStreamA: " + (Arrays.equals(baos.toByteArray(), backup) && rv));
    }

    private static void test_writeArrayToStreamB(Streams s) {
        Random r = new Random();
        byte[] data = new byte[1024];
        r.nextBytes(data);
        byte[] backup = Arrays.copyOf(data, 1024);
        int start = r.nextInt(data.length * 9 / 10);
        int length = r.nextInt(data.length - start);
        byte[] backup_trunc = new byte[length];
        System.arraycopy(backup, start, backup_trunc, 0, length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean rv = s.writeArrayToStream(data, start, length, baos);
        System.out.println("writeArrayToStreamB: " + (Arrays.equals(baos.toByteArray(), backup_trunc) && rv));
    }

    private static void test_writeInputToOutput(Streams s) {
        Random r = new Random();
        byte[] data = new byte[1024];
        r.nextBytes(data);
        byte[] backup = Arrays.copyOf(data, 1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        boolean rv = s.writeInputToOutput(bais, baos);
        System.out.println("writeInputToOutput: " + (Arrays.equals(baos.toByteArray(), backup) && rv));
    }

    private static void test_writeUTF8TextToStream(Streams s) {
        String data = "~~~ ╔͎═͓═͙╗\n~~~ ╚̨̈́═̈́﴾ ̥̂˖̫˖̥  ̂ )";
        byte[] backup = new byte[]{126, 126, 126, 32, -30, -107, -108, -51, -114, -30, -107, -112, -51, -109, -30, -107, -112, -51, -103, -30, -107, -105, 10, 126, 126, 126, 32, -30, -107, -102, -51, -124, -52, -88, -30, -107, -112, -51, -124, -17, -76, -66, 32, -52, -126, -52, -91, -53, -106, -52, -85, -53, -106, -52, -91, -30, -128, -118, -30, -128, -118, -52, -126, -30, -128, -118, 41};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean rv = s.writeUTF8TextToStream(data, baos);
        System.out.println("writeUTF8TextToStream: " + (Arrays.equals(backup, baos.toByteArray()) && rv));
    }

    private static void test_writeEncodedTextToStream(Streams s) {
        String data = "~~~ ╔͎═͓═͙╗\n~~~ ╚̨̈́═̈́﴾ ̥̂˖̫˖̥  ̂ )";
        byte[] backup = new byte[]{-2, -1, 0, 126, 0, 126, 0, 126, 0, 32, 37, 84, 3, 78, 37, 80, 3, 83, 37, 80, 3, 89, 37, 87, 0, 10, 0, 126, 0, 126, 0, 126, 0, 32, 37, 90, 3, 68, 3, 40, 37, 80, 3, 68, -3, 62, 0, 32, 3, 2, 3, 37, 2, -42, 3, 43, 2, -42, 3, 37, 32, 10, 32, 10, 3, 2, 32, 10, 0, 41};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean rv = s.writeEncodedTextToStream(data, StandardCharsets.UTF_16, baos);
        System.out.println("writeEncodedTextToStream: " + (Arrays.equals(backup, baos.toByteArray()) && rv));
    }

    private static void test_asPrintStream(Streams s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream rv = s.asPrintStream(baos);
        if (rv != null) {
            rv.print("Test Text");
            rv.flush();
        }
        System.out.println("asPrintStream: " + (rv != null && "Test Text".equals(baos.toString(StandardCharsets.UTF_8))));
    }

    private static void test_writeToPrintStream(Streams s) {
        Random r = new Random();
        byte[] data = new byte[1024];
        r.nextBytes(data);
        String text = "test text";
        byte[] backup = new byte[1024 + text.length() + System.lineSeparator().length()];
        byte[] bytes = (text + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytes, 0, backup, 0, bytes.length);
        System.arraycopy(data, 0, backup, bytes.length, data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        PrintStream ps = new PrintStream(baos, false, StandardCharsets.UTF_8);
        boolean rv = s.writeToPrintStream(ps, text, bais);
        ps.flush();
        System.out.println("writeToPrintStream: " + (Arrays.equals(baos.toByteArray(), backup) && rv));
    }

    private static void test_parseParam2(Streams s) {
        LinkedHashMap<String, Double> tests = new LinkedHashMap<>();
        tests.put("command --param1 text --param2 123.456 --param3 asdf", 123.456d);
        tests.put("andererCommand --param2 456.123", 456.123d);
        tests.put("command --param3 Einhorn", Double.NaN);
        tests.put("", Double.NaN);
        int i = 0;
        for (var t : tests.entrySet()) {
            i++;
            double d = s.parseParam2(t.getKey());
            System.out.println("parseParam2 Test " + i + ": " + (Double.compare(d, t.getValue()) == 0));
        }
    }

    private static void test_asBufferedReader(Streams s) {
        ByteArrayInputStream bais = new ByteArrayInputStream(("Some Test Text"+System.lineSeparator()+"And some more Unicorns."+System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        BufferedReader rv = s.asBufferedReader(bais);
        String a = null;
        String b = null;
        try {
            if (rv != null) {
                long waitStart = System.currentTimeMillis();
                while (!rv.ready() && waitStart + 1e3 < System.currentTimeMillis()) Thread.sleep(1);
                a = rv.readLine();
                b = rv.readLine();
            }
        }
        catch (IOException ioexc)
        {
            System.err.println("Running the 'asBufferedReader'-Test failed! Received an unexpected IOException:");
            ioexc.printStackTrace();
        }
        catch (InterruptedException iexc)
        {
            System.err.println("Running the 'asBufferedReader'-Test failed! Received an unexpected Interrupt from another Thread ...");
        }
        System.out.println("asBufferedReader: " + (rv != null && ("Some Test Text").equals(a) && "And some more Unicorns.".equals(b)));
    }

    private static void test_sum(Streams s) {
        StringBuilder data = new StringBuilder();
        long sum = 0;
        Random r = new Random();
        for (int i = 0; i < 256; i++) {
            long l = r.nextInt();
            data.append(l).append(System.lineSeparator());
            sum += l;
        }
        data.append("end").append(System.lineSeparator());
        for (int i = 0; i < 256; i++) {
            long l = r.nextInt();
            data.append(l).append(System.lineSeparator());
        }
        BufferedReader br = new BufferedReader(new CharArrayReader(data.toString().toCharArray()));
        long rv = s.sum(br);
        try {
            br.close();
        } catch (IOException ignore) {
        }
        System.out.println("sum: " + (sum == rv));
    }

    private static void test_getIntList(Streams s) {
        StringBuilder data = new StringBuilder();
        ArrayList<Integer> input = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < 256; i++) {
            int l = r.nextInt(128);
            data.append("Es liegen ").append(l).append(" Früchte im Korb").append(System.lineSeparator());
            input.add(l);
        }
        data.append("end").append(System.lineSeparator());
        for (int i = 0; i < 256; i++) {
            long l = r.nextInt();
            data.append("Es liegen ").append(l).append(" Früchte im Korb").append(System.lineSeparator());
        }
        BufferedReader br = new BufferedReader(new CharArrayReader(data.toString().toCharArray()));
        List<Integer> rv = s.getIntList(br);
        try {
            br.close();
        } catch (IOException ignore) {
        }
        boolean result = rv != null && (rv.size() == input.size());
        for (int i = 0; result && i < input.size(); i++) {
            result = Objects.equals(input.get(i), rv.get(i));
        }
        System.out.println("getIntList: " + (result));
    }
}
