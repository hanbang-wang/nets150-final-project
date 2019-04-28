import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

final class DataProcessing {
    static final private String url = "https://oracleofbacon.org/data.txt.bz2";
    static final private ObjectMapper mapper = new ObjectMapper();
    static private URL dataSet = null;
    final private List<Util.MovieInfo> movieInfos = new LinkedList<>();

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
        URLConnection urlConnection = dataSet.openConnection(new Proxy(Proxy.Type.HTTP, new Socket("127.0.0.1", 8888).getRemoteSocketAddress()));
        InputStream inputStream = urlConnection.getInputStream();
        BZip2CompressorInputStream stream = new BZip2CompressorInputStream(inputStream);
        try (Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                Util.MovieInfo info = mapper.readValue(scanner.nextLine(), Util.MovieInfo.class);
                movieInfos.add(info);
            }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        System.err.printf("Acquired %d listings.\n", movieInfos.size());
    }

    List<String[]> getAllCasts() {
        List<String[]> ret = new LinkedList<>();
        movieInfos.forEach(i -> ret.add(i.cast));
        return ret;
    }

    List<Util.MovieInfo> getMovieInfos() {
        return movieInfos;
    }
}
