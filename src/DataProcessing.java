import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

final class DataProcessing {
    static final private String url = "https://oracleofbacon.org/data.txt.bz2";
    static final private ObjectMapper mapper = new ObjectMapper();
    static private URL dataSet = null;
    final private List<MovieInfo> movieInfos = new LinkedList<>();

    DataProcessing() {
        if (dataSet == null) {
            try {
                dataSet = new URL(url);
            } catch (MalformedURLException ignored) {
            }
        }
    }

    void acquireData() throws IOException {
        System.err.println("Start connecting...");
        URLConnection urlConnection = dataSet.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        BZip2CompressorInputStream stream = new BZip2CompressorInputStream(inputStream);
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                MovieInfo info = mapper.readValue(scanner.nextLine(), MovieInfo.class);
                movieInfos.add(info);
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        System.err.printf("Acquired %d listings.\n", movieInfos.size());
    }

    List<List<String>> getAllCasts() {
        List<List<String>> ret = new LinkedList<>();
        movieInfos.forEach(i -> ret.add(Collections.unmodifiableList(i.cast)));
        return ret;
    }
}
