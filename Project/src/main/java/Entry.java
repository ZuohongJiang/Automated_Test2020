
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;


import java.io.*;

import java.util.*;



public class Entry {
    static AnalysisScope scope;
    static CallGraph cg;
    static HashMap<String, HashSet<String >> methodDependencies = new HashMap<String, HashSet<String>>();//存方法依赖，value为所有依赖key方法的方法签名的集合
    static HashMap<String, HashSet<String>> classDependencies = new HashMap<String, HashSet<String>>();//存类依赖，value为所有依赖key类的类类签名集合
    static HashMap<String, String> MethodClassPair = new HashMap<String, String>();//存储方法-类键值对
    static HashMap<String, Integer> TestMethodMark = new HashMap<String, Integer>();//若为String所表示的方法为测试方法，则值为1
    static HashSet<String> ImpactedTests = new HashSet<String>();//存储受影响的测试方法签名


    public static void main(String[] args) {
        Command command = new Command(args[0].charAt(1), args[1], args[2]);//根据运行jar包时传入参数构造命令
        File exclusion = new File("exclusion.txt");
        String targetPath = command.getProjectTarget();
        File target = new File(targetPath);
        String ScopePath = "scope.txt";
        ClassLoader classLoader = Entry.class.getClassLoader();
        try {
            scope = AnalysisScopeReader.readJavaScope(ScopePath, exclusion, classLoader);
            MyUtil.traverseFolder(target, scope); //遍历target下所有class文件并加入分析域
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidClassFileException e) {
            e.printStackTrace();
        }
        try {
            ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);//生成类层次关系对象
            Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);//生成进入点
            CHAbuildCg(cha, eps); //构建CHA调用图

            aggregateInfo();
            MethodChange methodChange
                    = new MethodChange(command.getChangeInfo());
            if(command.getArgument() == 'm')
                MethodSelect(methodChange.getChangeInfo());
            else if(command.getArgument() == 'c')
                ClassSelect(methodChange.getChangeInfo());
//            makeDotFile();
        } catch (ClassHierarchyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void CHAbuildCg(ClassHierarchy cha,Iterable<Entrypoint> eps){
        cg = new CHACallGraph(cha, true);
        try {
            ((CHACallGraph) cg).init(eps);
        } catch (CancelException e) {
            e.printStackTrace();
        }
    }


    /**
     * 遍历CHA生成的调用图中的所有节点，挑选出所有使用Application类加载器加载的方法，使用MethodClassPair存储方法与类的对应信息，
     * 使用TestMethodMark记录方法是否为测试方法(通过读取注解判断方法是否为测试方法），调用addDependencies记录依赖
     */
    public static void aggregateInfo(){
        for(CGNode node:cg)
        {
            if(node.getMethod() instanceof ShrikeBTMethod){
                ShrikeBTMethod method = (ShrikeBTMethod)node.getMethod();

                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())){
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    if(!MethodClassPair.containsKey(signature))
                        MethodClassPair.put(signature,classInnerName);
                    if(!TestMethodMark.containsKey(signature))
                        TestMethodMark.put(signature,0);

                    Iterator <Annotation> annotationIterator = method.getAnnotations().iterator();
                    if(annotationIterator.hasNext())
                        {
                            String name = annotationIterator.next().getType().getName().getClassName().toString();
                            if(name.equals("Test"))
                                TestMethodMark.put(signature,1);
                        }
                    addDependencies(classInnerName,signature,cg.getPredNodes(node));
                }
            }
        }
    }

    /**
     * 使用classDependencies存储类依赖关系，使用methodDependencies存储方法依赖关系
     * @param classInnerName 类签名
     * @param signature 方法签名
     * @param It 存储了所有调用过  signature 的方法对应节点的迭代器
     */
    public static void addDependencies(String classInnerName, String signature,Iterator<CGNode> It) {
        if (!classDependencies.containsKey(classInnerName))
            classDependencies.put(classInnerName, new HashSet<String>());
        if (!methodDependencies.containsKey(signature))
            methodDependencies.put(signature, new HashSet<String>());
        while (It.hasNext())
        {
            CGNode caller = It.next();
            classDependencies.get(classInnerName).add(caller.getMethod().getDeclaringClass().getName().toString());
            methodDependencies.get(signature).add(caller.getMethod().getSignature());
        }
    }

    /**
     * 生成.dot文件的方法
     */
    public static void makeDotFile(){
        try{
            BufferedWriter MethodOut = new BufferedWriter(new FileWriter("method-NextDay-cha.dot"));

            MethodOut.write("digraph cmd_method{" + "\n");
            for(String key: methodDependencies.keySet()){
                Iterator It = methodDependencies.get(key).iterator();
                while(It.hasNext()){
                    MethodOut.write("\"" + key + "\"" + " -> " + "\"" + It.next() + "\"" + ";" + "\n");
                }
            }
            MethodOut.write("}");
            MethodOut.flush();
            MethodOut.close();

            BufferedWriter ClassOut = new BufferedWriter(new FileWriter("class-NextDay-cha.dot"));

            ClassOut.write("digraph cmd_class{" + "\n");
            for(String key: classDependencies.keySet()){
                Iterator It = classDependencies.get(key).iterator();
                while(It.hasNext()){
                    ClassOut.write("\"" + key + "\"" + " -> " + "\"" + It.next() + "\"" + ";" + "\n");
                }
            }
            ClassOut.write("}");
            ClassOut.flush();
            ClassOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法级别测试用例选择，对于发生改变的方法，要选出所有依赖该方法的方法的测试方法
     * @param changeInfo
     * @throws IOException
     */
    public static void MethodSelect(HashMap<String, String> changeInfo) throws IOException {

        BufferedWriter MethodOut = new BufferedWriter(new FileWriter("selection-method.txt"));

        //利用CHA生成的调用图，只能获得通过直接调用产生的依赖。例如A<-B<-C这种方式，A<-C需要自己通过递归方式记录
        for(String key: changeInfo.keySet())
            recurrenceAddTestMethod(key);

        if(ImpactedTests.isEmpty())
        {
            System.out.println("No Impacted tests!");
            return;
        }
        Iterator It = ImpactedTests.iterator();
        writeImpactedTests(It,MethodOut);
        MethodOut.flush();
        MethodOut.close();
    }

    public static void recurrenceAddTestMethod(String methodSig){
        Iterator It = methodDependencies.get(methodSig).iterator();
        while(It.hasNext()){
            String methodSignature = It.next().toString();
            if(TestMethodMark.get(methodSignature) == 1){
                ImpactedTests.add(MethodClassPair.get(methodSignature) + " " + methodSignature);
            }
            // 对于递归方法，不递归计算自身的依赖，否则会产生死循环
            else if(!methodSig.equals(methodSignature))
                recurrenceAddTestMethod(methodSignature);
        }
    }

    /**
     * 类级测试用例选择，对于发生改变的类，要选出所有依赖改类的类的所有测试方法
     * @param changeInfo
     * @throws IOException
     */
    public static void ClassSelect(HashMap<String, String> changeInfo) throws IOException {
        BufferedWriter MethodOut = new BufferedWriter(new FileWriter("selection-class.txt"));

        HashSet<String> ImpactedClasses = new HashSet<String>();
        for(String classSig: changeInfo.values()){
            Iterator It = classDependencies.get(classSig).iterator();
            while(It.hasNext()){
                ImpactedClasses.add(It.next().toString());
            }
        }
        for(String classSig: ImpactedClasses){
            for(String methodSig: MethodClassPair.keySet()){
                if(MethodClassPair.get(methodSig).equals(classSig)){
                    if(TestMethodMark.get(methodSig) == 1)
                        ImpactedTests.add(classSig + " " + methodSig);
                }
            }
        }
        if(ImpactedTests.isEmpty())
        { System.out.println("No Impacted tests!");
            return;
        }
        Iterator It = ImpactedTests.iterator();
        writeImpactedTests(It,MethodOut);
        MethodOut.flush();
        MethodOut.close();
    }

    public static void writeImpactedTests(Iterator It, BufferedWriter MethodOut) throws IOException {
        while(It.hasNext()){
            MethodOut.write(It.next().toString());
            MethodOut.write("\n");
        }
    }
}
