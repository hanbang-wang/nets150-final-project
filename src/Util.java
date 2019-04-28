import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class Util {
    static final class Pair<T1, T2> {
        final T1 first;
        final T2 second;

        Pair(T1 f, T2 s) {
            first = f;
            second = s;
        }
    }

    static final public class MovieInfo {
        public String title;
        public String year;
        public String[] cast;
        public String[] directors;
        public String[] producers;
        public String[] companies;
    }
}
