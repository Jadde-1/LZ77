import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class LZ77 {

    private static final int WINDOW_SIZE = 8192; // Window size in bytes
    private static final int BUFFER_SIZE = 255;  // Lookahead buffer size in bytes

    public static void compressFile(String inputFilePath, String outputFilePath) {
        try {
            Long startTime = System.nanoTime();
            byte[] content = readBinaryFile(inputFilePath);

            Long endStartTime = System.nanoTime();
            long loadTime = endStartTime - startTime;
            long originalSize = content.length;

            Long compressStart = System.nanoTime();
            LZ77Token[] tokens = compress(content);
            Long compressEnd = System.nanoTime();
            long compressTime = compressEnd - compressStart;

            Long saveStart = System.nanoTime();
            saveCompressedFile(outputFilePath, tokens, content.length);
            Long saveEnd = System.nanoTime();
            long saveTime = saveEnd - saveStart;

            long compressedSize = new File(outputFilePath).length();
            int savedPct = (int) ((originalSize - compressedSize) * 100.0 / originalSize);

            System.out.println("Original size: " + originalSize + " bytes");
            System.out.println("Compressed size: " + compressedSize + " bytes");
            System.out.println("Savings: " + savedPct + "%\n");
            System.out.println("Compression completed");
            System.out.println("\nLoadTime: " + loadTime / 1000000 + "ms" +
                               "\nCompressTime: " + compressTime / 1000000 + "ms" +
                               "\nSaveTime: " + saveTime / 1000000 + "ms");
        } catch (Exception e) {
            System.out.println("Error during compression");
        }
    }

    public static void decompressFile(String inputFilePath, String outputFilePath) {
        try {
            Long startTime = System.nanoTime();
            CompressedFileData data = loadCompressedFile(inputFilePath);
            Long loadEnd = System.nanoTime();
            long loadTime = loadEnd - startTime;

            long compressedSize = new File(inputFilePath).length();

            Long decompressStart = System.nanoTime();
            byte[] decompressed = decompress(data.tokens, data.originalLength);
            Long decompressEnd = System.nanoTime();
            long decompressTime = decompressEnd - decompressStart;

            Long writeStart = System.nanoTime();
            writeBinaryFile(outputFilePath, decompressed);
            Long writeEnd = System.nanoTime();
            long writeTime = writeEnd - writeStart;

            if (decompressed.length == data.originalLength) {
                System.out.println("File decompressed correctly");
            } else {
                System.out.println("Error in decompression");
            }

            System.out.println("Compressed size: " + compressedSize + " bytes");
            System.out.println("Decompressed size: " + decompressed.length + " bytes");
            System.out.println("\nLoadTime: " + loadTime / 1000000 + "ms" +
                               "\nDecompressTime: " + decompressTime / 1000000 + "ms" +
                               "\nWriteTime: " + writeTime / 1000000 + "ms");
        } catch (Exception e) {
            System.out.println("Error during decompression");
        }
    }

    private static LZ77Token[] compress(byte[] input) {
        int maxTokens = input.length;
        LZ77Token[] tokens = new LZ77Token[maxTokens];
        int tokenCount = 0;
        int position = 0;
        int length = input.length;
        int compressedSize = 0;

        while (position < length) {
            int bestMatchLength = 0;
            int bestMatchDistance = 0;

            int searchStart = Math.max(0, position - WINDOW_SIZE);
            int maxMatch = Math.min(BUFFER_SIZE, length - position);

            for (int i = searchStart; i < position; i++) {
                int matchLen = 0;
                while (matchLen < maxMatch &&
                       i + matchLen < position &&
                       input[i + matchLen] == input[position + matchLen]) {
                    matchLen++;
                }
                if (matchLen >= bestMatchLength) {
                    bestMatchLength = matchLen;
                    bestMatchDistance = position - i;
                    if (matchLen == maxMatch) break;
                }
            }

            if (compressedSize + 4 >= input.length) {
                System.out.println("File cannot be compressed. Too many tokens");
                return null;
            }

            if (bestMatchLength > 2) {
                byte nextByte = (position + bestMatchLength < length)
                                ? input[position + bestMatchLength] : 0;
                tokens[tokenCount++] = new LZ77Token(bestMatchDistance, bestMatchLength, nextByte);
                position += bestMatchLength + 1;
                compressedSize += 4;
            } else {
                tokens[tokenCount++] = new LZ77Token(0, 0, input[position]);
                position++;
                compressedSize += 4;
            }
        }

        return Arrays.copyOf(tokens, tokenCount);
    }

    private static byte[] decompress(LZ77Token[] tokens, int originalLength) {
        byte[] result = new byte[originalLength];
        int currentPosition = 0;

        for (LZ77Token token : tokens) {
            if (token.distance == 0) {
                result[currentPosition++] = token.nextByte;
            } else {
                int start = currentPosition - token.distance;
                for (int i = 0; i < token.length; i++) {
                    result[currentPosition++] = result[start + i];
                }
                if (token.nextByte != 0 && currentPosition < originalLength) {
                    result[currentPosition++] = token.nextByte;
                }
            }
        }

        return result;
    }

    private static byte[] readBinaryFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        long fileSize = Files.size(filePath);

        if(fileSize > Integer.MAX_VALUE){
            throw new IOException("File is too large.");
        }

        return Files.readAllBytes(filePath);
    }

    private static void writeBinaryFile(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }

    private static void saveCompressedFile(String path, LZ77Token[] tokens, int originalLength) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {
            out.writeInt(originalLength);
            out.writeInt(tokens.length);
            for (LZ77Token t : tokens) {
                out.writeShort(t.distance);
                out.writeByte(t.length);
                out.writeByte(t.nextByte & 0xFF);
            }
        }
    }

    private static CompressedFileData loadCompressedFile(String path) throws IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(path))) {
            int originalLength = in.readInt();
            int tokenCount = in.readInt();
            LZ77Token[] tokens = new LZ77Token[tokenCount];
            for (int i = 0; i < tokenCount; i++) {
                short distance = in.readShort();
                byte length = in.readByte();
                byte nextByte = in.readByte();
                tokens[i] = new LZ77Token(distance, length, nextByte);
            }
            return new CompressedFileData(tokens, originalLength);
        }
    }

    private static class LZ77Token {
        final short distance;
        final byte length;
        final byte nextByte;

        LZ77Token(int distance, int length, byte nextByte) {
            this.distance = (short) distance;
            this.length = (byte) length;
            this.nextByte = (byte) nextByte;
        }
    }

    private static class CompressedFileData {
        final LZ77Token[] tokens;
        final int originalLength;

        CompressedFileData(LZ77Token[] tokens, int originalLength) {
            this.tokens = tokens;
            this.originalLength = originalLength;
        }
    }
}
```
