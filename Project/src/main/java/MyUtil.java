import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MyUtil {
    /**
     * 遍历target文件夹构建分析域
     * @param file
     * @param scope
     * @throws InvalidClassFileException
     */
    public static void traverseFolder(File file, AnalysisScope scope) throws InvalidClassFileException {

        if(file != null) {
            if (file.isDirectory()) {
                File[] fileList = file.listFiles();
                for(File subFile: fileList)
                    traverseFolder(subFile,scope);
            } else {
                if (file.getName().endsWith(".class"))
                {
                    scope.addClassFileToScope(ClassLoaderReference.Application,file);
                }
            }
        }
    }
    public static HashSet<String> getFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        HashSet<String> fileSet = new HashSet<String>();
        String temp;
        while((temp = bufferedReader.readLine()) != null){
            fileSet.add(temp);
        }
        return fileSet;
    }

}
