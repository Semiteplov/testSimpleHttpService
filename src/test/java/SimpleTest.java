import io.restassured.response.Response;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

public class SimpleTest extends RestAssuredConfig {
    private static final File TEMPLATE_IMAGE = new File("src/test/resources/templateImage.png");
    private static final File OUTPUT_IMAGE = new File("src/test/resources/output.png");

    @Test
    public void getDelayWithValidValue() {
        REQUEST.when().
                    get("/delay/5").
                then().
                    statusCode(200).
                    time(lessThan(6L), TimeUnit.SECONDS).
                    time(greaterThanOrEqualTo(5L), TimeUnit.SECONDS);
    }

    // Max delay is 10 seconds.
    @Test
    public void getDelayWithBigValue() {
        REQUEST.when().
                    get("/delay/150").
                then().
                    statusCode(200).
                    time(lessThan(12L), TimeUnit.SECONDS).
                    time(greaterThanOrEqualTo(10L), TimeUnit.SECONDS);
    }

    @Test
    public void getDelayWithSmallValue() {
        REQUEST.when().
                    get("/delay/-32").
                then().
                    statusCode(200).
                    time(lessThan(2L), TimeUnit.SECONDS).
                    time(greaterThanOrEqualTo(1L), TimeUnit.SECONDS);
    }

    @Test
    public void getDelayWithInvalidValue() {
        REQUEST.when().
                    get("/delay/test").
                then().
                    statusCode(500);
    }

    @Test
    public void getImagePNGAndCompareToTemplate() throws IOException {
        REQUEST.when().
                    get("/image/png").
                then().
                    statusCode(200).
                    header("content-type", "image/png");

        Response response = REQUEST.get("/image/png");

        writeImage(response.getBody().asInputStream());

        assertTrue(compareImages(TEMPLATE_IMAGE, OUTPUT_IMAGE));
    }

    public static void writeImage(InputStream inputStream) throws IOException {
        // opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(OUTPUT_IMAGE);

        int bytesRead = -1;
        byte[] buffer = new byte[8090];

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();
    }

    public static boolean compareImages(File file1, File file2) {
        try {
            // take buffer data
            BufferedImage bi1 = ImageIO.read(file1);
            DataBuffer db1 = bi1.getData().getDataBuffer();
            int size1 = db1.getSize();

            BufferedImage bi2 = ImageIO.read(file2);
            DataBuffer db2 = bi2.getData().getDataBuffer();
            int size2 = db2.getSize();

            // compare data buffer objects
            if (size1 == size2) {
                for (int i = 0; i < size1; i++) {
                    if (db1.getElem(i) != db2.getElem(i)) {
                        return false;
                    }
                }
                return true;
            } else
                return false;
        } catch (Exception e) {
            System.out.println("Failed to compare image");
            return  false;
        }
    }
}
