import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        // Variable to track whether the user chose compression or decompression
        int choice = -1;

        while (choice != 0) {
            // Print menu options
            System.out.println("\n-LZ77 Tool-\n");
            System.out.println("1 - Compress file");
            System.out.println("2 - Decompress file");
            System.out.println("3 - Exit");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    compress();
                    break;

                case 2:
                    deCompress();
                    break;

                // Exit the program, wait 2 seconds, then close
                case 3:
                    System.out.println("Program closing...");
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                    break;
            }
        }
        scanner.close();
    }

    private static void compress() {
        System.out.print("Input file path: ");
        String inputFile = scanner.nextLine();

        System.out.print("\nChange the file extension if you do not want to overwrite the existing file\nOutput file path: ");
        String outputFile = scanner.nextLine();


        LZ77.compressFile(inputFile, outputFile);
    }

    private static void deCompress() {
        System.out.print("Change the file extension if you do not want to overwrite the existing file\nEnter path to .lz77 file: ");
        String inputFile = scanner.nextLine();

        System.out.print("Enter path for output .txt file: ");
        String outputFile = scanner.nextLine();


        LZ77.decompressFile(inputFile, outputFile);
    }
}

