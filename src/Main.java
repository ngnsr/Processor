import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    // AC -- accumulator,
// IR - current command,
// R1,R2,R3,R4 - registers,
// PS = +/-, sign of the last operation,
// PC - count of commands,
// TC - count of ticks
// commands:
//
// mov(num) | mov(Rx) -- move value to the A
// save(Rx)
// addm(Rx) -- Кожний байт 1-го операнда окремо складається по модулю 2 з молодшим байтом 2-го операнда, представленого у
    private static final int BITS = 30;
    private static final int REGS = 4;
    private static int PC = 1;
    private static int TC;
    private static boolean PS;

    private static String AC;
    private static String IR;

    private static String[][] registers;

    public static void main(String[] args) {
//        initRandomRegisters();
        executeCommands();
//        printState();
    }

    private static void executeCommands() {
        File file = new File("/Users/rsnhn/IdeaProjects/Processor/src/input");
        if (!file.exists() || !file.canRead()) {
            System.err.println("Impossible to open the file");
            System.exit(1);
        }

        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().trim().toLowerCase().split("\s");
                String command = parts[0];
                String operand = parts[1];

                if (command.equals("mov") && (operand.matches("\\d+") || operand.matches("R[1-4]"))) {
                    mov(operand);
                } else if (command.equals("save") && operand.matches("R[1-4]")) {
                    save(operand);
                } else if (command.equals("addm") && operand.matches("R[1-4]")) {
                    addm(operand);
                } else
                    System.exit(0);
                PC++;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addm(String operand) {

    }

    private static void save(String operand) {

    }

    private static void mov(String operand) {

    }

    private static void printState() {
        printRegisters();
        System.out.println("PS: " + (PS ? "-" : "+"));
        System.out.println("IR: " + IR);
        System.out.println("PC: " + PC);
        System.out.println("TC: " + TC);
    }

    private static void initRandomRegisters() {
        IR = "";
        registers = new String[REGS][BITS];
        TC = 1;
        Random r = new Random();
        PS = r.nextBoolean();

        for (int i = 0; i < REGS; i++) {
            for (int j = 0; j < BITS; j++) {
                registers[i][j] = (r.nextBoolean() ? "1" : "0");
            }
        }
    }

    private static void printRegisters() {
        for (int i = 0; i < REGS; i++) {
            System.out.print("R" + (i + 1) + ": ");
            for (int j = 0; j < BITS; j++) {
                System.out.print(registers[i][j]);
                if (j % 8 == BITS % 8 - 1)
                    System.out.print(" ");
            }
            System.out.println();
        }
    }


}