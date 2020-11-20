import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class testRes {
    @Test
    public void  testMethod() throws IOException {
        HashSet<String> testSet = MyUtil.getFile("C:\\Users\\12736\\Desktop\\学习\\自动化\\经典自动化大作业\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\1-ALU\\data\\selection-method.txt");
        HashSet<String> resSet = MyUtil.getFile("selection-method.txt");
        assertEquals(resSet.size(),testSet.size());
        for(String s: testSet){
            assert(resSet.contains(s));
        }
    }

    @Test
    public void testClass() throws IOException {
        HashSet<String> testSet = MyUtil.getFile("C:\\Users\\12736\\Desktop\\学习\\自动化\\经典自动化大作业\\ClassicAutomatedTesting\\ClassicAutomatedTesting\\1-ALU\\data\\selection-class.txt");
        HashSet<String> resSet = MyUtil.getFile("selection-class.txt");
        assertEquals(resSet.size(),testSet.size());
        for(String s: testSet){
            assert(resSet.contains(s));
        }
    }
}
