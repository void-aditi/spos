// Pass2Assembler.java
// Generates Pass-2 output in exactly the required format.
// Works on Linux: javac Pass2Assembler.java && java Pass2Assembler > pass2.txt

import java.io.*;
import java.util.*;

public class Pass2Assembler {

    static class Symbol {
        String sym, sName;
        int addr;
        Symbol(String sym, String sName, int addr) {
            this.sym = sym; this.sName = sName; this.addr = addr;
        }
    }

    static class Literal {
        String lit; int addr;
        Literal(String lit, int addr) { this.lit = lit; this.addr = addr; }
    }

    static Map<Integer, String> opcodeMap = new HashMap<>();
    static {
        for (int i = 0; i <= 20; i++)
            opcodeMap.put(i, String.format("%02d", i));
    }

    public static void main(String[] args) throws Exception {

        // ---------- Read IC ----------
        List<String> IC = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("IC.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("*") || line.startsWith("LC")) continue;
                IC.add(line);
            }
        }

        // ---------- Read SYMTAB ----------
        List<Symbol> SYMTAB = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SYMTAB.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.toUpperCase().contains("SYMBOL")) continue;
                String[] t = line.split("\\s+");
                if (t.length >= 3)
                    SYMTAB.add(new Symbol(t[0], t[1], Integer.parseInt(t[2])));
            }
        }

        // ---------- Read LITTAB ----------
        List<Literal> LITTAB = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("LITTAB.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.toUpperCase().contains("LITERAL")) continue;
                String[] t = line.split("\\s+");
                if (t.length >= 3)
                    LITTAB.add(new Literal(t[1], Integer.parseInt(t[2])));
            }
        }

        // ---------- Print Header ----------
        System.out.printf("%-7s %-45s %-20s%n", "LC", "INTERMEDIATE CODE", "LC\tMACHINE CODE");
        System.out.println("----------------------------------------------------------------------------");

        // ---------- Generate MC ----------
        for (String raw : IC) {
            String lc = "";
            String ic = raw;
            String mc = "-";

            // extract LC if present
            String[] parts = raw.split("\\s+", 2);
            if (parts.length > 0 && parts[0].matches("\\d+")) {
                lc = parts[0];
                if (parts.length == 2) ic = parts[1];
            }

            if (ic.contains("(AD")) {
                mc = "-";
            } else if (ic.contains("(DL")) {
                String val = "000";
                int s = ic.indexOf("(C,");
                if (s != -1)
                    val = ic.substring(s + 3, ic.indexOf(")", s));
                mc = "00 00 " + val;
            } else if (ic.contains("(IS")) {
                String opcode = "00", reg = "00", addr = "000";

                try {
                    int s = ic.indexOf("(IS,") + 4;
                    int e = ic.indexOf(")", s);
                    int num = Integer.parseInt(ic.substring(s, e));
                    opcode = opcodeMap.get(num);
                } catch (Exception ignore) {}

                String[] tokens = ic.split("\\)");
                for (String t : tokens) {
                    if (!t.contains("(")) continue;
                    t = t.substring(t.indexOf("(") + 1);
                    String[] sub = t.split(",");
                    if (sub.length < 2) continue;
                    String tag = sub[0].trim(), val = sub[1].trim();

                    if (tag.equalsIgnoreCase("RG") || tag.equalsIgnoreCase("R"))
                        reg = (val.length() == 1) ? "0" + val : val;
                    else if (tag.equalsIgnoreCase("S")) {
                        for (Symbol s : SYMTAB)
                            if (s.sName.equalsIgnoreCase(val))
                                addr = String.valueOf(s.addr);
                    } else if (tag.equalsIgnoreCase("L")) {
                        try {
                            int i = Integer.parseInt(val.substring(1)) - 1;
                            addr = String.valueOf(LITTAB.get(i).addr);
                        } catch (Exception ignore) {}
                    } else if (tag.equalsIgnoreCase("C")) addr = val;
                }
                mc = opcode + " " + reg + " " + addr;
            }

            String fullMC = (mc.equals("-") || lc.isEmpty()) ? mc : lc + "\t" + mc;
            System.out.printf("%-7s %-45s %-20s%n", lc, ic, fullMC);
        }

        System.out.println("\nProgram finished execution.");
    }
}
