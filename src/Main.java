import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {


        IndexStructure str = new IndexStructure("D:\\.Education\\alg_lab2\\");
//        for(int i = 0; i < 10_000; i++){
//            str.set(i+"", (int)(Math.random()*100_000)+"");
//        }

            System.out.println(str.get("665"));
    }
}
