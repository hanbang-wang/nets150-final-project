import org.junit.Test;

import javax.xml.crypto.Data;

import java.io.IOException;

import static org.junit.Assert.*;

public class DataProcessingTest {
    private DataProcessing process = new DataProcessing();

    @Test
    public void acquireData() {
        try {
            process.acquireData();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}