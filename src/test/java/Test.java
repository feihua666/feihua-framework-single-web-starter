/**
 * Created by yangwei
 * Created at 2018/7/27 20:36
 */
public class Test {
    public static void main(String[] args) {
        String a = "sdsdfs${ssss}";
        System.out.println(a.substring(a.indexOf("${"),a.indexOf("}") + 1));
    }
}
