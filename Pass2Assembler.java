import java.io.*;
import java.util.*;

public class Pass2Assembler {

    static class Symbol {
        String symName;   // actual symbol (like LOOP)
        String sName;     // symbolic name (like S1)
        int addr;

        Symbol(String symName, String sName, int addr) {
            this.symName = symName;
            this.sName = sName;
            this.addr = addr;
        }
    }

    static class Literal {
        String lit;
        int addr;

        Literal(String l, int a) {
            lit = l;
            addr = a;
        }
    }

    // opcode table (two-digit strings)
    static Map<Integer, String> opcodeMap = new HashMap<>();
    static {
        for (int i = 0; i <= 20; i++)
            opcodeMap.put(i, String.format("%02d", i));
    }

    public static void main(String[] args) throws Exception {
        // -------------------- Read IC --------------------
        List<String> icLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("IC.txt"))) {
            String line;
            boolean skipHeader = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (skipHeader && line.startsWith("LC")) { skipHeader = false; continue; }
                if (!line.isEmpty() && !line.startsWith("*")) icLines.add(line);
            }
        }

        // -------------------- Read SYMTAB --------------------
        List<Symbol> SYMTAB = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("SYMTAB.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (!line.matches("^\\d+.*")) continue; // skip headers and stars
                String[] tok = line.split("\\s+");
                if (tok.length >= 4) {
                    String sym = tok[1];
                    String sName = tok[2];
                    int addr = Integer.parseInt(tok[3]);
                    SYMTAB.add(new Symbol(sym, sName, addr));
                }
            }
        }

        // -------------------- Read LITTAB --------------------
        List<Literal> LITTAB = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("LITTAB.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (!line.matches("^\\d+.*")) continue; // skip headers and stars
                String[] tok = line.split("\\s+");
                if (tok.length >= 3) {
                    String lit = tok[1];
                    int addr = Integer.parseInt(tok[2]);
                    LITTAB.add(new Literal(lit, addr));
                }
            }
        }

        // -------------------- Generate Machine Code --------------------
        System.out.printf("%-6s %-45s %-20s%n", "LC", "INTERMEDIATE CODE", "MACHINE CODE");
        System.out.println("----------------------------------------------------------------------------");

        for (String raw : icLines) {
            String lcStr = "";
            String icPart = raw;

            // extract LC if present
            String[] split = raw.split("\\s+", 2);
            if (split.length >= 1 && split[0].matches("\\d+")) {
                lcStr = split[0];
                if (split.length == 2) icPart = split[1].trim();
            }

            String mc = "-";

            if (icPart.contains("(AD")) mc = "-"; // assembler directive → no MC
            else if (icPart.contains("(DL")) {    // declarative
                String constVal = null;
                if (icPart.contains("(C,")) {
                    constVal = icPart.substring(icPart.indexOf("(C,") + 3,
                            icPart.indexOf(")", icPart.indexOf("(C,")));
                }
                mc = "00 00 " + (constVal == null ? "000" : constVal);
            } else if (icPart.contains("(IS")) { // imperative
                String opcode = "00", reg = "00", addr = "000";

                // extract opcode
                try {
                    int start = icPart.indexOf("(IS,") + 4;
                    int end = icPart.indexOf(")", start);
                    int opnum = Integer.parseInt(icPart.substring(start, end));
                    opcode = opcodeMap.getOrDefault(opnum, String.format("%02d", opnum));
                } catch (Exception ignore) {}

                // extract operands
                String[] tokens = icPart.split("\\)");
                for (String t : tokens) {
                    if (t.contains("(")) t = t.substring(t.indexOf("(") + 1);
                    String[] sub = t.split(",", 2);
                    if (sub.length != 2) continue;

                    String tag = sub[0].trim();
                    String val = sub[1].trim();

                    if (tag.equalsIgnoreCase("RG") || tag.equalsIgnoreCase("R"))
                        reg = val.length() == 1 ? "0" + val : val;
                    else if (tag.equalsIgnoreCase("S")) {
                        // lookup symbol address from SYMTAB
                        for (Symbol s : SYMTAB) {
                            if (s.sName.equalsIgnoreCase(val)) { addr = String.valueOf(s.addr); break; }
                        }
                    } else if (tag.equalsIgnoreCase("L")) {
                        // lookup literal address from LITTAB
                        try {
                            int idx = Integer.parseInt(val.substring(1)) - 1;
                            if (idx >= 0 && idx < LITTAB.size()) addr = String.valueOf(LITTAB.get(idx).addr);
                        } catch (Exception ignore) {}
                    } else if (tag.equalsIgnoreCase("C")) addr = val;
                }

                mc = opcode + " " + reg + " " + addr;
            }

            System.out.printf("%-6s %-45s %-20s%n", lcStr, icPart, mc);
        }
    }
}
