import java.util.Arrays;

/**
 * Created by chenguo on 9/10/15.
 */
public class Test {

    public static void main(String[] args) throws Exception {

        String s = "Segment Id";
        byte[] bytes = s.getBytes("utf-8");
        System.out.println(Character.toChars(83));
        System.out.println(Arrays.toString(bytes));

        byte[] outputByte = new byte[2];
        outputByte[0] = 83;
        //outputByte[0] = 99;

        System.out.println(outputByte.toString());
    }
}
