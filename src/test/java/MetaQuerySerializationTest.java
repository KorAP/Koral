import de.ids_mannheim.korap.query.serialize.MetaCollectionSerializer;
import de.ids_mannheim.korap.query.serialize.MetaQuerySerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hanl
 * @date 04/12/2013
 */

@RunWith(JUnit4.class)
public class MetaQuerySerializationTest {

    private MetaQuerySerializer serializer;

    public MetaQuerySerializationTest() {
        serializer = new MetaQuerySerializer();
    }

    @Test
    public void test() throws IOException {
        Map<String, String> j = new HashMap();
        j.put("author", "Goethe");
        j.put("pubPLace", "Erfurt");
        j.put("textClass", "wissenschaft");
        String s = serializer.stringify(j);
//        System.out.println("value reference " + s);
    }

    @Test
    public void testSingle() throws IOException {
        Map<String, String> j = new HashMap();
        j.put("textClass", "wissenschaft");
        String s = serializer.stringify(j);
//        System.out.println("value reference test single " + s);
    }

    @Test
    public void testResourceMeta() throws IOException {
        MetaCollectionSerializer ser = new MetaCollectionSerializer();
        String s = ser.serialize("25");
        System.out.println(" --- RESULT JSON " + s);


    }
}
