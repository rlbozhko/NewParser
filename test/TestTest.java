import java.util.regex.Pattern;

public class TestTest {

    @org.junit.Test
    public void testRegular() {
          final Pattern ROZETKA_CATEGORY =  Pattern.compile(".*/c\\d*(/filter/|/)");
        //Pattern.compile(".*/c[0-9]*/filter/|.*/c[0-9]*/");
        //Pattern.compile(".*/c[0-9]*((/filter/)|(/))");
        // http://rozetka.com.ua/mobile-phones/c80003/filter/
        // "http://rozetka.com.ua/mobile-phones/apple/c80003/v069/"
        String href = "http://rozetka.com.ua/mobile-phones/c80003/";
        if (ROZETKA_CATEGORY.matcher(href).matches()) {
            System.out.println("YES");

        } else{
            System.out.println("NOO");
        }
    }


    @org.junit.Test
    public void testMap() {




    }
}