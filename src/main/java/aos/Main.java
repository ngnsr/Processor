package aos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class Main {

// AC -- accumulator,
// IR - current command,
// r1,r2,r3,r4 - registers,
// PS = +/-, sign of the last operation,
// PC - count of commands,
// TC - count of ticks
// commands:
// mov(num) | mov(rx) -- move value to the AC
// save(rx) -- save value to AC
// addm(num) | addm(rx) -- Each byte of the 1st operand is separately added modulo 2 with the low byte of the 2nd operand.
    private static final int BITS = 26;
    private static final int REGS = 4;
    private static int PC = 1;
    private static int TC = 0;
    private static boolean PS;
    private static String AC;
    private static String IR;

    private static String[] registers;

    public static void main(final String[] args) {
        initRandomRegisters();
        executeCommands();
    }

    private static void initRandomRegisters() {
        registers = new String[REGS];
        final Random r = new Random();
        PS = r.nextBoolean();
        IR = "";

        StringBuilder sb = new StringBuilder(BITS);
        for (int i = 0; i < REGS; i++) {

            for (int j = 0; j < BITS; j++)
                sb.append(r.nextBoolean() ? "1" : "0");
            registers[i] = sb.toString();
            sb.setLength(0);
        }
        AC = "0".repeat(BITS);
    }
    private static void executeCommands() {
        final File file = new File("/Users/rsnhn/IdeaProjects/Processor/src/input");
        if (!file.exists() || !file.canRead()) {
            System.err.println("Impossible to open the file");
            System.exit(1);
        }

        try {
            final Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                final String[] parts = sc.nextLine().trim().toLowerCase().split("\\s+");
                if (parts.length != 2)
                    continue;
                final String command = parts[0];
                final String operand = parts[1];
                executeCommand(command, operand);
                PC++;
                waitEnter();
            }
            sc.close();
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeCommand(String command, String operand) {
        if (command.equals("save") && (operand.matches("[+-]?\\d+") || operand.matches("r[1-4]"))) {
            save(operand);
        } else if (command.equals("mov") && operand.matches("r[1-4]")) {
            mov(operand);
        } else if (command.equals("addm") && (operand.matches("[+-]?\\d+") || operand.matches("r[1-4]"))) {
            addm(operand);
        } else {
            System.out.printf("Unknown command [ %s ], skip\n", command + " " + operand);
            // System.exit(1);
        }
    }

    final static Scanner s = new Scanner(System.in);
    private static void waitEnter() {
        s.nextLine();
            // System.in.read();
    }

    private static void save(final String operand) {
        IR = "save" + " | " + operand;
        printStateAndUpdateTC(1);
        waitEnter();

        if (operand.matches("[+-]?\\d+")) {
            int n = Integer.parseInt(operand);
            AC = toBin(n);
        } else { // "r[1-4]"
            int registerNumber = Integer.parseInt(String.valueOf(operand.charAt(1))) - 1;
            AC = registers[registerNumber];
        }

        PS = AC.startsWith("1");
        printStateAndUpdateTC(2);
    }

    private static void mov(final String operand) {
        IR = "mov" + " | " + operand;
        printStateAndUpdateTC(1);
        waitEnter();
        int registerNumber = Integer.parseInt(String.valueOf(operand.charAt(1))) - 1;
        registers[registerNumber] = AC;
        PS = AC.startsWith("1");
        printStateAndUpdateTC(2);
    }

    private static void addm(final String operand) {
        IR = "addm" + " | " + operand;
        printStateAndUpdateTC(1);
        waitEnter();

        String[] ACBytes = formatRegister(AC).split(" ");
        String[] regBytes;
        if (operand.matches("[+-]?\\d+")) {
            regBytes = formatRegister(toBin(Integer.parseInt(operand))).split(" ");
        } else { // "r[1-4]"
            int registerNumber = Integer.parseInt(String.valueOf(operand.charAt(1))) - 1;
            regBytes = formatRegister(registers[registerNumber]).split(" ");
        }

        StringBuilder sb = new StringBuilder(BITS);
        String lowByte = regBytes[regBytes.length - 1];
        int lowBit = lowByte.charAt(lowByte.length() - 1) == '1' ? 1 : 0;
        int byteIndex = 0;

        // ???
        // if(BITS % 8 > 0){
        //     byteIndex = 1;
        //     sb.append(AC.substring(0, BITS % 8));
        // }

        for (; byteIndex < regBytes.length; byteIndex++) {
            int numberBits = ACBytes[byteIndex].length();
            int currentLowBit = ACBytes[byteIndex].charAt(numberBits - 1) == '1' ? 1 : 0;
            int m = lowBit ^ currentLowBit;
            String mString = toBin(m);
            sb.append(mString, mString.length() - numberBits, BITS);
        }
        AC = sb.toString();
        PS = AC.startsWith("1");
        printStateAndUpdateTC(2);
    }

    private static void printStateAndUpdateTC(int tc) {
        printRegisters();
        setTC(tc);
        System.out.println("IR: " + IR);
        System.out.println("PS: " + (PS ? "-" : "+"));
        System.out.println("PC: " + PC);
        System.out.println("TC: " + TC);
    }

    private static void printRegisters() {
        for (int i = 0; i < REGS; i++)
            System.out.println("r" + (i + 1) + ": " + formatRegister(registers[i]));
        System.out.println("AC: " + formatRegister(AC));
    }

    private static String formatRegister(String register) {
        StringBuilder sb = new StringBuilder(BITS);

        for (int j = 0; j < BITS; j++) {
            sb.append(register.charAt(j));
            if (j % 8 == BITS % 8 - 1)
                sb.append(" ");
        }
        return sb.toString();
    }

    private static void setTC(int tc){
        TC = tc;
    }

    private static String toBin(int n) {
        if (n > Math.pow(2, BITS - 1) - 1) {
            System.out.println("Number is to large! [ " + n + " ]");
            System.exit(1);
        } else if (n < -Math.pow(2, BITS - 1)) {
            System.out.println("Number is to small! [ " + n + " ]");
            System.exit(1);
        }
        boolean negative = n < 0;
        n = Math.abs(n);
        StringBuilder sb = new StringBuilder(BITS);
        while (n > 0) {
            sb.append(n % 2);
            n /= 2;
        }

        sb = new StringBuilder("0".repeat(BITS - sb.length()) + sb.reverse());
        if (negative) {
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == '0') {
                    sb.setCharAt(i, '1');
                } else sb.setCharAt(i, '0');
            }
            int lastZeroIndex = sb.toString().lastIndexOf('0');
            return (lastZeroIndex == -1) ? "0".repeat(BITS)
                    : sb.substring(0, lastZeroIndex) + "1" + "0".repeat(BITS - lastZeroIndex);
        }
        return sb.toString();
    }

}
