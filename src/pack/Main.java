package pack;

import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.file.StandardOpenOption.*;

public class Main {
    public static void main(String[] args) {
        //Понаписал может лишнего, но все проверил вроде работает корректно

        Path file = Path.of("D:\\Test\\test.pdf");
        Path copy = Path.of("D:\\Test\\copy.pdf");
        Path nextCopy = Path.of("D:\\Test\\nextCopy.pdf"); // это для второго варианта решения

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // Это я так понимаю более правильный подход, но правда с помощью гугла (ниже вариант написаный самостоятельно )
        try (FileChannel reader = FileChannel.open(file, READ);
             FileChannel writer = FileChannel.open(copy, CREATE_NEW, WRITE)) {

            while (reader.read(buffer) > 0 || buffer.position() != 0) {
                buffer.flip();
                writer.write(buffer);
                buffer.clear();
            }
            reader.close();
            writer.close();
            System.out.println("\nFile copied successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Это вариант написаный самостоятельно, но тут не используеться канал чтения
        try (FileChannel writer = FileChannel.open(nextCopy, CREATE_NEW, WRITE)) {

            byte[] bytes = Files.readAllBytes(file);
         //   System.out.println("Length: " + bytes.length);
            int cursor = 0;
            while (cursor < bytes.length) {
                buffer.clear();
                buffer.put(bytes, cursor, Math.min(buffer.limit(), bytes.length - cursor));
                cursor += buffer.position();
                buffer.flip();

                writer.write(buffer);
            }
            System.out.println("\nFile copied successfully");

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Просчет хеш-суммы копируемых файлов, тоже спасибо гуглу (пришлось добавить 2 библы)

        try {

            byte[] fileContent = FileUtils.readFileToByteArray(file.toFile());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileContent);
            String hashString = new String(Hex.encodeHex(hash));
            System.out.println("Хеш-сумма файла: " + hashString);

            byte[] fileContentCopy = FileUtils.readFileToByteArray(copy.toFile());
            byte[] hashCopy = digest.digest(fileContentCopy);
            String hashStringCopy = new String(Hex.encodeHex(hashCopy));
            System.out.println("Хеш-сумма копии файла: " + hashStringCopy);

            byte[] fileContentNextCopy = FileUtils.readFileToByteArray(nextCopy.toFile());
            byte[] hashNextCopy = digest.digest(fileContentNextCopy);
            String hashStringNextCopy = new String(Hex.encodeHex(hashNextCopy));
            System.out.println("Хеш-сумма следующей копии файла: " + hashStringNextCopy);

            System.out.println(hashString.equals(hashStringCopy)&&hashString.equals(hashStringNextCopy));

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }



    }
}